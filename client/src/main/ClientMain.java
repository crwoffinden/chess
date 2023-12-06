import chess.*;
import com.google.gson.*;
import game.Position;
import model.Game;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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

    public static void main(String[] args) throws IOException {
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
                                ChessGame yourGame = gamesList[id].getGame();
                                drawBoard(out, yourGame, ChessGame.TeamColor.WHITE);
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
                                ChessGame yourGame = gamesList[id].getGame();
                                drawBoard(out, yourGame, ChessGame.TeamColor.BLACK);
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
                            //Gets the game and draws the board from both points of view
                            ChessGame yourGame = gamesList[id].getGame();
                            drawBoard(out, yourGame, ChessGame.TeamColor.WHITE);
                            out.print(SET_BG_COLOR_BLACK);
                            out.print("\n");
                            drawBoard(out, yourGame, ChessGame.TeamColor.BLACK);
                        }
                        else out.print(joinRes.getMessage() + "\n");
                        break;
                    default: out.print("I didn't recognize that. Try something else.\n");
                }
            }
            out.print("What would you like to do?");
        }
    }

    //Draws the board on the UI
    public static void drawBoard(PrintStream out, ChessGame game, ChessGame.TeamColor color) {
        //keeps track of the color of the squares
        // (since the bottom left corner is always black, the top left corner is always white)
        boolean white = true;
        //prints the column letters
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print("   ");
        //orientation depends on the point of view
        if (color == ChessGame.TeamColor.WHITE) {
            for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                char colName = (char) ('a' + col - 1);
                out.print(" " + colName + " ");
            }
        }
        else {
            for (int col = 8; col > 0; --col) {
                char colName = (char) ('a' + col - 1);
                out.print(" " + colName + " ");
            }
        }
        out.print("   ");
        out.print("\n");
        //Orientation depends on the point of view
        if (color == ChessGame.TeamColor.WHITE) {
            for (int row = 8; row > 0; --row) {
                out.print(SET_BG_COLOR_DARK_GREEN);
                //Prints row number
                out.print(" " + row + " ");
                for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                    //prints the correct colored square
                    if (white) out.print(SET_BG_COLOR_LIGHT_GREY);
                    else out.print(SET_BG_COLOR_DARK_GREY);
                    //Finds the piece on that spot and prints accordingly
                    ChessPiece piece = game.getBoard().getPiece(new Position(row, col));
                    if (piece == null) out.print("   ");
                    //Uses letters because for some reason, unicode black pawns are wider than the other unicode pieces
                    //White pieces
                    else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        out.print(SET_TEXT_COLOR_WHITE);
                        switch (piece.getPieceType()) {
                            case PAWN:
                                out.print(" P ");
                                break;
                            case ROOK:
                                out.print(" R ");
                                break;
                            case KNIGHT:
                                out.print(" N ");
                                break;
                            case BISHOP:
                                out.print(" B ");
                                break;
                            case QUEEN:
                                out.print(" Q ");
                                break;
                            case KING:
                                out.print(" K ");
                                break;
                            default: out.print("   ");
                        }
                    }
                    //Black Pieces
                    else if(piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        out.print(SET_TEXT_COLOR_BLACK);
                        switch (piece.getPieceType()) {
                            case PAWN:
                                out.print(" P ");
                                break;
                            case ROOK:
                                out.print(" R ");
                                break;
                            case KNIGHT:
                                out.print(" N ");
                                break;
                            case BISHOP:
                                out.print(" B ");
                                break;
                            case QUEEN:
                                out.print(" Q ");
                                break;
                            case KING:
                                out.print(" K ");
                                break;
                            default: out.print("   ");
                        }
                    }
                    //Alternates the color of the next square
                    white = (!white);
                }
                out.print(SET_BG_COLOR_DARK_GREEN);
                out.print(SET_TEXT_COLOR_BLACK);
                //Prints the row number
                out.print(" " + row + " ");
                //Alternates the color of the first square on the next row
                white = (!white);
                out.print("\n");
            }
        }
        if (color == ChessGame.TeamColor.BLACK) {
            for (int row = 1; row <= BOARD_SIZE_IN_SQUARES; ++row) {
                out.print(SET_BG_COLOR_DARK_GREEN);
                //Prints the row number
                out.print(" " + row + " ");
                for (int col = 8; col > 0; --col) {
                    //Ensures the right square color
                    if (white) out.print(SET_BG_COLOR_LIGHT_GREY);
                    else out.print(SET_BG_COLOR_DARK_GREY);
                    //Finds the piece and prints accordingly
                    ChessPiece piece = game.getBoard().getPiece(new Position(row, col));
                    if (piece == null) out.print("   ");
                    //White pieces
                    else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        out.print(SET_TEXT_COLOR_WHITE);
                        switch (piece.getPieceType()) {
                            case PAWN:
                                out.print(" P ");
                                break;
                            case ROOK:
                                out.print(" R ");
                                break;
                            case KNIGHT:
                                out.print(" N ");
                                break;
                            case BISHOP:
                                out.print(" B ");
                                break;
                            case QUEEN:
                                out.print(" Q ");
                                break;
                            case KING:
                                out.print(" K ");
                                break;
                            default:
                                out.print("   ");
                        }
                    }
                    //Black pieces
                    else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        out.print(SET_TEXT_COLOR_BLACK);
                        switch (piece.getPieceType()) {
                            case PAWN:
                                out.print(" P ");
                                break;
                            case ROOK:
                                out.print(" R ");
                                break;
                            case KNIGHT:
                                out.print(" N ");
                                break;
                            case BISHOP:
                                out.print(" B ");
                                break;
                            case QUEEN:
                                out.print(" Q ");
                                break;
                            case KING:
                                out.print(" K ");
                                break;
                            default:
                                out.print("  ");
                        }
                    }
                    //Alternates the color of the next square
                    white = (!white);
                }
                out.print(SET_BG_COLOR_DARK_GREEN);
                out.print(SET_TEXT_COLOR_BLACK);
                //Prints the row number
                out.print(" " + row + " ");
                //Alternates the color of the first square of the next row
                white = (!white);
                out.print("\n");
            }
        }
        out.print(SET_BG_COLOR_DARK_GREEN);
        out.print(SET_TEXT_COLOR_BLACK);
        out.print("   ");
        //Prints the column letters
        //Orientation depends on point of view
        if (color == ChessGame.TeamColor.WHITE) {
            for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                char colName = (char) ('a' + col - 1);
                out.print(" " + colName + " ");
            }
        }
        else {
            for (int col = 8; col > 0; --col) {
                char colName = (char) ('a' + col - 1);
                out.print(" " + colName + " ");
            }
        }
        out.print("   ");
        out.print("\n");
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }
}
