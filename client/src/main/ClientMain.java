import chess.*;
import com.google.gson.*;
import game.Move;
import game.Position;
import model.Game;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.*;
import ui.DrawBoard;
import userGameCommandClasses.*;
import webSocketMessages.UserGameCommand;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static ui.EscapeSequences.*;

public class ClientMain {
    //To draw the board
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    //Default help screen when logged out
    private static final String LOGGED_OUT_HELP_SCREEN = "register -> create a new account\n" +
            "login -> log in to your account\nhelp -> display possible commands\nquit -> quit program\n";

    //Default help screen when logged in
    private static final String LOGGED_IN_HELP_SCREEN = "create -> create a new game\nlist -> list all games\n" +
            "join -> join an existing game\nobserve -> spectate an ongoing game\nlogout -> log out of your account\n" +
            "help -> display possible commands\nquit -> quit program\n";

    private static final String IN_GAME_HELP_SCREEN = "redraw -> redraws the board\nmove -> make a move\n" +
            "highlight -> highlight all legal moves for a piece\nresign -> forfeit the game\n" +
            "leave -> leave the game\nhelp -> display possible commands\n";

    private static final String OBSERVING_GAME_HELP_SCREEN = "redraw -> redraws the board\nleave -> leave the game\n" +
            "help -> display possible commands\n";
    //The server facade
    private static final ServerFacade SERVER = new ServerFacade();

    //Adapters
    static class PositionAdapter implements JsonDeserializer<ChessPosition> {

        @Override
        public ChessPosition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Position.class);
        }
    }

    static class MoveAdapter implements JsonDeserializer<ChessMove> {

        @Override
        public ChessMove deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Move.class);
        }
    }

    static class PieceAdapter implements JsonDeserializer<ChessPiece> {

        @Override
        public ChessPiece deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Piece.class);
        }
    }

    static class BoardAdapter implements JsonDeserializer<ChessBoard> {

        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Board.class);
        }
    }
    static class GameAdapter implements JsonDeserializer<ChessGame> {

        @Override
        public ChessGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Game.class);
        }
    }

    //Gson Builder
    private static final GsonBuilder BUILDER = new GsonBuilder();

    public static void main(String[] args) throws Exception {
        //Registers the adapters
        BUILDER.registerTypeAdapter(ChessPosition.class, new PositionAdapter());
        BUILDER.registerTypeAdapter(ChessMove.class, new MoveAdapter());
        BUILDER.registerTypeAdapter(ChessPiece.class, new PieceAdapter());
        BUILDER.registerTypeAdapter(ChessBoard.class, new BoardAdapter());
        BUILDER.registerTypeAdapter(ChessGame.class, new GameAdapter());
        //Saves the current authtoken for the session
        String authtoken = null;
        //Url string
        String url = "http://localhost:8080";
        Gson gson = BUILDER.create();

        //Saves the list of games
        Game[] gamesList = null;

        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Scanner scanner = new Scanner(System.in);
        out.print(ERASE_SCREEN);
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print("Welcome to Chess. Type \"help\" to see commands.");
        //Used to determine what menus to display and what options are vaild
        boolean loggedIn = false;

        while (scanner.hasNext()) {
            //User's input
            String arg = scanner.next();
            if (!loggedIn) {
                //Options when logged out
                switch (arg.toLowerCase()) {
                    case ("help") :
                        //Displays options
                        out.print(LOGGED_OUT_HELP_SCREEN);
                        break;
                    case ("quit") :
                        //Leaves
                        out.print("Goodbye\n");
                        return;
                    case ("login") :
                        out.print("Username: ");
                        String username = scanner.next();
                        out.print("Password: ");
                        String password = scanner.next();
                        //Logs the user in
                        LoginRequest l = new LoginRequest(username, password);
                        String json = gson.toJson(l);
                        String responseJSON = SERVER.postRequest(url + "/session" ,json, authtoken);
                        LoginResult loginRes = gson.fromJson(responseJSON, LoginResult.class);
                        authtoken = loginRes.getAuthToken();
                        loggedIn = loginRes.isSuccess();
                        if (loggedIn) out.print("Success! You are logged in. Type \"help\" to see commands\n");
                        else out.print(loginRes.getMessage() + "\n");
                        break;
                    case ("register") :
                        //Registers a new user
                        out.print("Username: ");
                        username = scanner.next();
                        out.print("Password: ");
                        password = scanner.next();
                        out.print("Email: ");
                        String email = scanner.next();
                        RegisterRequest r = new RegisterRequest(username, password, email);
                        json = gson.toJson(r);
                        responseJSON = SERVER.postRequest(url + "/user", json, authtoken);
                        RegisterResult registerRes = gson.fromJson(responseJSON, RegisterResult.class);
                        authtoken = registerRes.getAuthToken();
                        loggedIn = registerRes.isSuccess();
                        if (loggedIn) out.print("Success! You have been registered and are logged in. " +
                                "Type \"help\" to see commands\n");
                        else out.print(registerRes.getMessage() + "\n");
                        break;
                    default: out.print("I didn't recognize that. Try something else.\n");
                }
            }
            else {
                //Valid options when logged in
                switch (arg.toLowerCase()) {
                    case ("help"):
                        //Displays options
                        out.print(LOGGED_IN_HELP_SCREEN);
                        break;
                    case ("quit"):
                        //Logs the user out and leaves
                        String responseJSON = SERVER.deleteRequest(url + "/session", authtoken);
                        LogoutResult logoutRes = gson.fromJson(responseJSON, LogoutResult.class);
                        if (logoutRes.isSuccess()) {
                            out.print("Goodbye\n");
                            loggedIn = false;
                            authtoken = null;
                            return;
                        }
                        else out.print("Error logging you out. Type \"quit\" again.\n");
                    case ("logout"):
                        //Logs the user out
                        responseJSON = SERVER.deleteRequest(url + "/session", authtoken);
                        logoutRes = gson.fromJson(responseJSON, LogoutResult.class);
                        if (logoutRes.isSuccess()) {
                            loggedIn = false;
                            authtoken = null;
                            out.print("You are now logged out. Type \"help\" to see commands\n");
                        }
                        else out.print(logoutRes.getMessage() + "\n");
                        break;
                    case ("create"):
                        out.print("Game Name:");
                        String name = scanner.next();
                        //Creates a new game
                        CreateGameRequest c = new CreateGameRequest(name);
                        String json = gson.toJson(c);
                        responseJSON = SERVER.postRequest(url + "/game", json, authtoken);
                        CreateGameResult createRes = gson.fromJson(responseJSON, CreateGameResult.class);
                        if (createRes.isSuccess()) out.print("Success! \"" + name +
                                "\" created. Type \"list\" to see your game\n");
                        else out.print(createRes.getMessage() + "\n");
                        break;
                    case ("list"):
                        //Lists all games and their players
                        responseJSON = SERVER.getRequest(url + "/game", authtoken);
                        ListGamesResult listRes = gson.fromJson(responseJSON, ListGamesResult.class);
                        if (listRes.isSuccess()) {
                            gamesList = listRes.getGames();
                            for (int i = 0; i < gamesList.length; ++i) {
                                StringBuilder gameString =
                                        new StringBuilder(i + ") " + gamesList[i].getGameName() + " Played By: ");
                                if (gamesList[i].getWhiteUsername() != null) {
                                    gameString.append(gamesList[i].getWhiteUsername());
                                }
                                gameString.append("(White) ");
                                if (gamesList[i].getBlackUsername() != null) {
                                    gameString.append(gamesList[i].getBlackUsername());
                                }
                                gameString.append("(Black) \n");
                                out.print(gameString.toString());
                            }
                        }
                        else out.print(listRes.getMessage() + "\n");
                        break;
                    case ("join"):
                        out.print("Game number:");
                        int id = Integer.parseInt(scanner.next());
                        if ((gamesList == null) || (id >= gamesList.length)) {
                            //Doesn't let a player join a game with an input id not on the list
                            out.print("Invalid id. Try again\n");
                            break;
                        }
                        out.print("Which color do you want to be? (w/b)");
                        String color = scanner.next().toLowerCase();
                        if (color.equals("w")) {
                            //Adds the user to the game as white
                            JoinGameRequest j =
                                    new JoinGameRequest(ChessGame.TeamColor.WHITE, gamesList[id].getGameID());
                            json = gson.toJson(j);
                            responseJSON = SERVER.putRequest(url + "/game", json, authtoken);
                            JoinGameResult joinRes = gson.fromJson(responseJSON, JoinGameResult.class);
                            if (joinRes.isSuccess()) {
                                //Gets the game, and draws the board from white's point of view
                                ChessGame yourGame = gamesList[id].getGame();//FIXME may be unneccessary
                                playGame(out, gamesList[id], authtoken, ChessGame.TeamColor.WHITE);
                            }
                            else out.print(joinRes.getMessage() + "\n");
                        }
                        else if (color.equals("b")) {
                            //Adds the user to the game as black
                            JoinGameRequest j =
                                    new JoinGameRequest(ChessGame.TeamColor.BLACK, gamesList[id].getGameID());
                            json = gson.toJson(j);
                            responseJSON = SERVER.putRequest(url + "/game", json, authtoken);
                            JoinGameResult joinRes = gson.fromJson(responseJSON, JoinGameResult.class);
                            if (joinRes.isSuccess()) {
                                //Gets the game and draws the board from black's point of view
                                ChessGame yourGame = gamesList[id].getGame(); //FIXME may be unneccessary
                                playGame(out, gamesList[id], authtoken, ChessGame.TeamColor.BLACK);
                            }
                            else out.print(joinRes.getMessage() + "\n");
                        }
                        //Doesn't add the user to the game if they put in an invalid color
                        else out.print("Invalid color selection. Try again.\n");
                        break;
                    case ("observe"):
                        out.print("Game number:");
                        id = Integer.parseInt(scanner.next());
                        //Doesn't let a player observe a game with an id number not on the list
                        if((gamesList == null) || (id >= gamesList.length)) {
                            out.print("Invalid id. Try again\n");
                            break;
                        }
                        //Adds the user to the game as an observer
                        JoinGameRequest j = new JoinGameRequest(null, gamesList[id].getGameID());
                        json = gson.toJson(j);
                        responseJSON = SERVER.putRequest(url + "/game", json, authtoken);
                        JoinGameResult joinRes = gson.fromJson(responseJSON, JoinGameResult.class);
                        if (joinRes.isSuccess()) {
                            observeGame(out, gamesList[id], authtoken);
                        }
                        else out.print(joinRes.getMessage() + "\n");
                        break;
                    default: out.print("I didn't recognize that. Try something else.\n");
                }
            }
            out.print("What would you like to do?");
        }
    }

    //For players
    public static void playGame(PrintStream out, Game game, String authToken, ChessGame.TeamColor color) throws Exception {
        ChessGame yourGame = game.getGame();
        //Sets up the webSocket Client
        WSClient rootClient = new WSClient();
        UserGameCommand joinCommand = new JoinPlayerUserGameCommand(authToken, game.getGameID(), color);
        Gson gson = BUILDER.create();
        String json = gson.toJson(joinCommand);
        rootClient.send(json);
        DrawBoard draw = new DrawBoard();
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print("\n");
        out.print("You are now playing " + game.getGameName() + ". Type \"help\" to see commands.");
        out.print("Your turn! What would you like to do?");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            //Ensures the game board being used is current
            Game[] gameList = new Game[0];
            String responseJSON = SERVER.getRequest("http://localhost:8080/game", authToken);
            ListGamesResult listRes = gson.fromJson(responseJSON, ListGamesResult.class);
            if (listRes.isSuccess()) {
                gameList = listRes.getGames();
            }
            int i = 0;
            while (i < gameList.length && gameList[i].getGameID() != game.getGameID()) ++i;
            if (i < gameList.length) yourGame = gameList[i].getGame();
            String arg = scanner.next();
            //menu
            switch (arg.toLowerCase()) {
                case ("help") :
                    out.print(IN_GAME_HELP_SCREEN);
                    break;
                case ("redraw") :
                    draw.draw(out, yourGame, color, null);
                    break;
                case ("leave") :
                    LeaveUserGameCommand leaveCommand = new LeaveUserGameCommand(authToken, game.getGameID());
                    json = gson.toJson(leaveCommand);
                    rootClient.send(json);
                    return;
                case ("resign") :
                    ResignUserGameCommand resignCommand = new ResignUserGameCommand(authToken, game.getGameID());
                    json = gson.toJson(resignCommand);
                    rootClient.send(json);
                    break;
                case ("move") :
                    out.print("Start square (letter, number, no spaces): ");
                        arg = scanner.next();
                        if (arg.length() == 2 && arg.toLowerCase().charAt(0) >= 'a' && arg.toLowerCase().charAt(0) <= 'h'
                                && arg.charAt(1) >= '1' && arg.charAt(1) <= '8') {
                            int col = (int) (arg.charAt(0) - 'a' + 1);
                            int row = (int) (arg.charAt(1) - '1' + 1);
                            ChessPosition startPos = new Position(row, col);
                            ChessPiece piece = yourGame.getBoard().getPiece(startPos);
                            //Ensures valid input
                            if (piece != null && piece.getTeamColor().equals(color)) {
                                out.print("End square (letter, number, no spaces): ");
                                arg = scanner.next();
                                if (arg.length() == 2 && arg.toLowerCase().charAt(0) >= 'a' && arg.toLowerCase().charAt(0) <= 'h'
                                        && arg.charAt(1) >= '1' && arg.charAt(1) <= '8') {
                                    int newCol = (int) (arg.charAt(0) - 'a' + 1);
                                    int newRow = (int) (arg.charAt(1) - '1' + 1);
                                    ChessPosition endPos = new Position(newRow, newCol);
                                    ChessMove proposedMove = new Move(startPos, endPos, null);
                                    MakeMoveUserGameCommand moveCommand =
                                            new MakeMoveUserGameCommand(authToken, game.getGameID(), proposedMove);
                                    json = gson.toJson(moveCommand);
                                    rootClient.send(json);
                                } else out.print("Invalid input, try again\n");
                            } else out.print("Choose a square with one of your pieces on it. Try again.\n");
                        } else out.print("Invalid input, try again.\n");
                    break;
                case ("highlight") :
                    out.print("Start square (letter, number, no spaces): ");
                    arg = scanner.next();
                    if (arg.length() == 2 && arg.toLowerCase().charAt(0) >= 'a' && arg.toLowerCase().charAt(0) <= 'h'
                            && arg.charAt(1) >= '1' && arg.charAt(1) <= '8') {
                        int col = (int) (arg.charAt(0) - 'a' + 1);
                        int row = (int) (arg.charAt(1) - '1' + 1);
                        ChessPosition startPos = new Position(row, col);
                        ChessPiece piece = yourGame.getBoard().getPiece(startPos);
                        if (piece != null && piece.getTeamColor().equals(color)) {
                            Collection<ChessMove> validMoves = yourGame.validMoves(startPos);
                            Set<ChessPosition> highlightSquares = new HashSet<ChessPosition>();
                            highlightSquares.add(startPos);
                            for (row = 1; row <= 8; ++row) {
                                for (col = 1; col <= 8; ++col) {
                                    ChessPosition endPos = new Position(row, col);
                                    if (validMoves.contains(new Move(startPos, endPos, null))) {
                                        highlightSquares.add(endPos);
                                    }
                                }
                            }
                            draw.draw(out, yourGame, color, highlightSquares);
                        }
                        else out.print("Choose a square with one of your pieces on it. Try again.\n");
                    }
                    else out.print("Invalid input, try again.\n");
                    break;
                default: out.print("I didn't understand that. Try again.\n");
            }
        }
    }

    //For observers
    public static void observeGame(PrintStream out, Game game, String authToken) throws Exception {
        ChessGame yourGame = game.getGame();
        //Sets up webSocket Client
        WSClient rootClient = new WSClient();
        UserGameCommand observeCommand = new JoinObserverUserGameCommand(authToken, game.getGameID());
        Gson gson = BUILDER.create();
        String json = gson.toJson(observeCommand);
        rootClient.send(json);
        DrawBoard draw = new DrawBoard();
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print("\n");
        out.print("You are now watching " + game.getGameName() + ". Type \"help\" to see commands.");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            //Ensures the game being observed is current
            Game[] gameList = new Game[0];
            String responseJSON = SERVER.getRequest("http://localhost:8080/game", authToken);
            ListGamesResult listRes = gson.fromJson(responseJSON, ListGamesResult.class);
            if (listRes.isSuccess()) {
                gameList = listRes.getGames();
            }
            int i = 0;
            while (i < gameList.length && gameList[i].getGameID() != game.getGameID()) ++i;
            if (i < gameList.length) yourGame = gameList[i].getGame();
            String arg = scanner.next();
            switch (arg.toLowerCase()) {
                //menu
                case ("help") :
                    out.print(OBSERVING_GAME_HELP_SCREEN);
                    break;
                case ("redraw") :
                    draw.draw(out, yourGame, null, null);
                case ("leave") :
                    LeaveUserGameCommand leaveCommand = new LeaveUserGameCommand(authToken, game.getGameID());
                    json = gson.toJson(leaveCommand);
                    rootClient.send(json);
                    return;
                default: out.print("I didn't understand that. Try again.\n");
            }
            out.print("What would you like to do?");
        }
    }
}
