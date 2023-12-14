package webSocket;

import chess.*;
import com.google.gson.*;
import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import model.AuthToken;
import model.Game;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import serverMessagesClasses.ErrorServerMessage;
import serverMessagesClasses.LoadGameServerMessage;
import serverMessagesClasses.NotificationServerMessage;
import spark.Spark;
import userGameCommandClasses.*;
import webSocketMessages.ServerMessage;
import webSocketMessages.UserGameCommand;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class WSServer {
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

    private ConnectionManager connections = new ConnectionManager();

    //Used to track which games are in progress and which games are over.
    private Map<Integer, Boolean> gameOver = new HashMap<Integer, Boolean>();

    public static void main(String[] args) {
        Spark.port(8081);
        Spark.webSocket("/connect", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
        //Registers the adapters
        BUILDER.registerTypeAdapter(ChessPosition.class, new PositionAdapter());
        BUILDER.registerTypeAdapter(ChessMove.class, new MoveAdapter());
        BUILDER.registerTypeAdapter(ChessPiece.class, new PieceAdapter());
        BUILDER.registerTypeAdapter(ChessBoard.class, new BoardAdapter());
        BUILDER.registerTypeAdapter(ChessGame.class, new GameAdapter());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        Gson gson = BUILDER.create();
        Database db = new Database();
        Connection conn = db.getConnection();
        //To compare gameID and authtoken
        GameDAO gDAO = new GameDAO(conn);
        AuthTokenDAO aDAO = new AuthTokenDAO(conn);

        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            UserGameCommand.CommandType type = command.getCommandType();
            String authtoken = command.getAuthString();
            AuthToken a = aDAO.find(authtoken);
            //ensures the authtoken is valid
            if (a != null) {
                String username = a.getUsername();
                switch (type) {
                    case JOIN_PLAYER:
                        JoinPlayerUserGameCommand joinCommand =
                                gson.fromJson(message, JoinPlayerUserGameCommand.class);
                        int gameID = joinCommand.getGameID();
                        Game g = gDAO.find(gameID);
                        //Ensures the game exists
                        if (g != null) {
                            webSocket.Connection newConn = new webSocket.Connection(gameID, username, session);
                            //adds the user's connection if it doesn't already exist
                            if (!connections.connections.contains(newConn)) {
                                connections.add(gameID, username, session);
                            }
                            //Ensures the spot being joined is in fact where the player is listed and not already taken
                            if ((joinCommand.getPlayerColor().equals(ChessGame.TeamColor.WHITE)
                                    && g.getWhiteUsername().equals(username))
                                    || (joinCommand.getPlayerColor().equals(ChessGame.TeamColor.BLACK)
                                    && g.getBlackUsername().equals(username))) {
                                //adds game to the game over tracker if it isn't there already
                                if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                                ChessGame.TeamColor color = joinCommand.getPlayerColor();
                                ChessGame game = gDAO.find(gameID).getGame();
                                LoadGameServerMessage loadMessage =
                                        new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                                String loadJson = gson.toJson(loadMessage);

                                String colorName;
                                if (color == ChessGame.TeamColor.WHITE) colorName = "white";
                                else colorName = "black";
                                String notificationString = username + " has joined the game as " + colorName;
                                NotificationServerMessage notification =
                                        new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                String notificationJson = gson.toJson(notification);
                                connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, loadJson);
                                connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, notificationJson);
                            } else {
                                ErrorServerMessage errorMessage =
                                        new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Color already taken. Try again.");
                                String json = gson.toJson(errorMessage);
                                session.getRemote().sendString(json);
                            }
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    case JOIN_OBSERVER:
                        JoinObserverUserGameCommand observerCommand =
                                gson.fromJson(message, JoinObserverUserGameCommand.class);
                        gameID = observerCommand.getGameID();
                        g = gDAO.find(gameID);
                        //ensures the game exists
                        if (g != null) {
                            webSocket.Connection newConn = new webSocket.Connection(gameID, username, session);
                            //adds the user's connection if it doesn't already exist
                            if (!connections.connections.contains(newConn)) {
                                connections.add(gameID, username, session);
                            }
                            //adds the game to the game over tracker if it isn't there already
                            if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                            ChessGame game = g.getGame();
                            LoadGameServerMessage loadMessage =
                                    new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                            String loadJson = gson.toJson(loadMessage);

                            String notificationString = username + " has joined the game as an observer";
                            NotificationServerMessage notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            String notificationJson = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, loadJson);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, notificationJson);
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    case MAKE_MOVE:
                        MakeMoveUserGameCommand moveCommand = gson.fromJson(message, MakeMoveUserGameCommand.class);
                        gameID = moveCommand.getGameID();
                        g = gDAO.find(gameID);
                        //ensures the game exists
                        if (g != null) {
                            //ensures the game is not over
                            if (!gameOver.get(gameID)) {
                                ChessGame game = g.getGame();
                                ChessMove move = moveCommand.getMove();
                                ChessPiece startPiece = game.getBoard().getPiece(move.getStartPosition());
                                //Ensures the move is not being made out of turn
                                if (startPiece != null) {
                                    ChessGame.TeamColor color = startPiece.getTeamColor();
                                    if (color.equals(game.getTeamTurn())) {
                                        if ((color == ChessGame.TeamColor.WHITE && g.getWhiteUsername().equals(username))
                                                || (color == ChessGame.TeamColor.BLACK
                                                && g.getBlackUsername().equals(username))) {
                                            //Ensures the move is valid
                                            if (game.validMoves(move.getStartPosition()).contains(move)) {
                                                game.makeMove(move);
                                                gDAO.update(gameID, game);
                                                LoadGameServerMessage loadMessage =
                                                        new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                                                String json = gson.toJson(loadMessage);
                                                connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                                //formatting for broadcast to everyone else
                                                int startRow = move.getStartPosition().getRow();
                                                char startCol = (char) ('a' + move.getStartPosition().getColumn() - 1);
                                                int endRow = move.getEndPosition().getRow();
                                                char endCol = (char) ('a' + move.getEndPosition().getColumn() - 1);
                                                char pieceType;
                                                switch (startPiece.getPieceType()) {
                                                    case PAWN -> pieceType = 'P';
                                                    case ROOK -> pieceType = 'R';
                                                    case KNIGHT -> pieceType = 'N';
                                                    case BISHOP -> pieceType = 'B';
                                                    case QUEEN -> pieceType = 'Q';
                                                    case KING -> pieceType = 'K';
                                                    default -> pieceType = '?';
                                                }
                                                String notificationString = username + " moved " + pieceType + startRow + startCol + endRow + endCol;
                                                NotificationServerMessage notification =
                                                        new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                                json = gson.toJson(notification);
                                                connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                                                //If in check
                                                if (game.isInCheck(game.getTeamTurn())) {
                                                    String opponentUsername;
                                                    if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
                                                        opponentUsername = gDAO.find(gameID).getWhiteUsername();
                                                    } else opponentUsername = gDAO.find(gameID).getBlackUsername();
                                                    notificationString = opponentUsername + " is in check.";
                                                    notification =
                                                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                                    json = gson.toJson(notification);
                                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                                    //If in checkmate
                                                    if (game.isInCheckmate(game.getTeamTurn())) {
                                                        notificationString = opponentUsername + " is in checkmate. " + username + " wins!";
                                                        notification =
                                                                new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                                        json = gson.toJson(notification);
                                                        connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                                        //marks the game as over
                                                        gameOver.remove(gameID);
                                                        gameOver.put(gameID, true);
                                                    }
                                                }
                                                //if in stalemate
                                                else if (game.isInStalemate(game.getTeamTurn())) {
                                                    notificationString = "Stalemate. It's a draw.";
                                                    notification =
                                                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                                    json = gson.toJson(notification);
                                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                                    //marks the game as over
                                                    gameOver.remove(gameID);
                                                    gameOver.put(gameID, true);
                                                }
                                            } else {
                                                String errorString = "Error: Invalid move! Try again.";
                                                ErrorServerMessage errorMessage =
                                                        new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                                String json = gson.toJson(errorMessage);
                                                connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                                            }
                                        } else {
                                            String errorString = "Error: Choose one of your pieces. Try again.";
                                            ErrorServerMessage errorMessage =
                                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                            String json = gson.toJson(errorMessage);
                                            connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                                        }
                                    } else {
                                        String errorString = "Error: Wait for your turn.";
                                        ErrorServerMessage errorMessage =
                                                new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                        String json = gson.toJson(errorMessage);
                                        connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                                    }
                                } else {
                                    String errorString = "Error: No piece to move. Try again.";
                                    ErrorServerMessage errorMessage =
                                            new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                    String json = gson.toJson(errorMessage);
                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                                }
                            } else {
                                String errorString = "Error: The game is over. Type \"leave\" to return to the menu.";
                                ErrorServerMessage errorMessage =
                                        new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                String json = gson.toJson(errorMessage);
                                connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                            }
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    case LEAVE:
                        LeaveUserGameCommand leaveCommand = gson.fromJson(message, LeaveUserGameCommand.class);
                        gameID = leaveCommand.getGameID();
                        g = gDAO.find(gameID);
                        //Ensures the game exists
                        if (g != null) {
                            //Removes the leaver's connection
                            connections.remove(username);
                            String notificationString = username + " left the game.";
                            NotificationServerMessage notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            String json = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    case RESIGN:
                        ResignUserGameCommand resignCommand = gson.fromJson(message, ResignUserGameCommand.class);
                        gameID = resignCommand.getGameID();
                        g = gDAO.find(gameID);
                        //Ensures the game exists
                        if (g != null) {
                            //Ensures the game is not over
                            if (!gameOver.get(gameID)) {
                                //Ensures that only a player can resign
                                if (username.equals(g.getWhiteUsername()) || username.equals(g.getBlackUsername())) {
                                    //For the message
                                    String opponentUsername = g.getWhiteUsername();
                                    if (opponentUsername.equals(username))
                                        opponentUsername = g.getBlackUsername();
                                    String notificationString = username + " resigned. " + opponentUsername + " wins!";
                                    NotificationServerMessage notification =
                                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                    String json = gson.toJson(notification);
                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                    //Marks the game as over
                                    gameOver.remove(gameID);
                                    gameOver.put(gameID, true);
                                } else {
                                    String errorString = "Error: You can't resign as an observer. Try again.";
                                    ErrorServerMessage errorMessage =
                                            new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                    String json = gson.toJson(errorMessage);
                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                                }
                            } else {
                                String errorString = "Error: The game is over. Type \"leave\" to return to the menu.";
                                ErrorServerMessage errorMessage =
                                        new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                                String json = gson.toJson(errorMessage);
                                connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                            }
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                ErrorServerMessage errorMessage =
                        new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Bad authtoken. Try again.");
                String json = gson.toJson(errorMessage);
                session.getRemote().sendString(json);
            }
        } catch (Exception e) {
            ErrorServerMessage errorMessage =
                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: " + e);
            String json = gson.toJson(errorMessage);
            db.closeConnection(conn);
            session.getRemote().sendString(json);
        }
        db.closeConnection(conn);
    }
}
