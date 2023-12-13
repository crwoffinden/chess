package webSocket;

import chess.*;
import com.google.gson.*;
import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
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

    private Map<Integer, Boolean> gameOver = new HashMap<Integer, Boolean>();

    public static void main(String[] args) {
        Spark.port(8080);
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
        GameDAO gDAO = new GameDAO(conn);
        AuthTokenDAO aDAO = new AuthTokenDAO(conn);
        try {
            System.out.printf("Received: %s", message);
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            UserGameCommand.CommandType type = command.getCommandType();
            String authtoken = command.getAuthString();
            String username = aDAO.find(authtoken).getUsername();
            switch (type) {
                case JOIN_PLAYER:
                    int gameID = ((JoinPlayerUserGameCommand) command).getGameID();
                    if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                    ChessGame.TeamColor color = ((JoinPlayerUserGameCommand) command).getPlayerColor();
                    ChessGame game = gDAO.find(gameID).getGame();
                    LoadGameServerMessage loadMessage =
                            new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                    String json = gson.toJson(loadMessage);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                    String colorName;
                    if (color == ChessGame.TeamColor.WHITE) colorName = "white";
                    else colorName = "black";
                    String notificationString = username + " has joined the game as " + colorName;
                    NotificationServerMessage notification =
                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                    json = gson.toJson(notification);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                    break;
                case JOIN_OBSERVER:
                    gameID = ((JoinObserverUserGameCommand) command).getGameID();
                    if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                    game = gDAO.find(gameID).getGame();
                    loadMessage =
                            new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                    json = gson.toJson(loadMessage);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                    notificationString = username + " has joined the game as an observer";
                    notification =
                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                    json = gson.toJson(notification);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                case MAKE_MOVE:
                    gameID = ((MakeMoveUserGameCommand) command).getGameID();
                    if (!gameOver.get(gameID)) {
                        game = gDAO.find(gameID).getGame();
                        ChessMove move = ((MakeMoveUserGameCommand) command).getMove();
                        if (game.validMoves(move.getStartPosition()).contains(move)) {
                            game.makeMove(move);
                            gDAO.update(gameID, game);
                            loadMessage =
                                    new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                            json = gson.toJson(loadMessage);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                            int startRow = move.getStartPosition().getRow();
                            char startCol = (char) ('a' + move.getStartPosition().getColumn() - 1);
                            int endRow = move.getEndPosition().getRow();
                            char endCol = (char) ('a' + move.getEndPosition().getColumn() - 1);
                            ChessPiece piece = game.getBoard().getPiece(move.getEndPosition());
                            char pieceType;
                            switch (piece.getPieceType()) {
                                case PAWN -> pieceType = 'P';
                                case ROOK -> pieceType = 'R';
                                case KNIGHT -> pieceType = 'N';
                                case BISHOP -> pieceType = 'B';
                                case QUEEN -> pieceType = 'Q';
                                case KING -> pieceType = 'K';
                                default -> pieceType = '?';
                            }
                            notificationString = username + " moved " + pieceType + startRow + startCol + endRow + endCol;
                            notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            json = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
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
                                if (game.isInCheckmate(game.getTeamTurn())) {
                                    notificationString = opponentUsername + " is in checkmate. " + username + " wins!";
                                    notification =
                                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                    json = gson.toJson(notification);
                                    connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
                                    gameOver.remove(gameID);
                                    gameOver.put(gameID, true);
                                }
                            }
                        }
                        else {
                            String errorString = "Error: Invalid move! Try again.";
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                            json = gson.toJson(errorMessage);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                        }
                    }
                    else {
                        String errorString = "Error: The game is over. Type \"leave\" to return to the menu.";
                        ErrorServerMessage errorMessage =
                                new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, errorString);
                        json = gson.toJson(errorMessage);
                        connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                    }
                    break;
                case LEAVE:
                    gameID = ((LeaveUserGameCommand)command).getGameID();
                    connections.remove(username);
                    if (gDAO.find(gameID).getWhiteUsername().equals(username)) {
                        gDAO.claimSpot(gameID, ChessGame.TeamColor.WHITE, null);
                    }
                    else if (gDAO.find(gameID).getBlackUsername().equals(username)) {
                        gDAO.claimSpot(gameID, ChessGame.TeamColor.BLACK, null);
                    }
                    notificationString = username + "left the game.";
                    notification =
                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                    json = gson.toJson(notification);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                case RESIGN:
                    gameID = ((ResignUserGameCommand)command).getGameID();
                    String opponentUsername = gDAO.find(gameID).getWhiteUsername();
                    if (opponentUsername.equals(username)) opponentUsername = gDAO.find(gameID).getBlackUsername();
                    notificationString = username + " resigned. " + opponentUsername + " wins!";
                    notification =
                            new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                    json = gson.toJson(notification);
                    connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                    gameOver.remove(gameID);
                    gameOver.put(gameID, true);
                    break;
                default: break;
            }
        } catch (Exception e) {
            ErrorServerMessage errorMessage =
                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Try again.");
            String json = gson.toJson(errorMessage);
            Integer gameID = null;
            String username = null;
            for (webSocket.Connection c : connections.connections.values()) {
                if (c.session.equals(session)) {
                    gameID = c.gameID;
                    username = c.username;
                }
            }
            connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
        }
        //session.getRemote().sendString("WebSocket response: " + message); //FIXME could be basic and wrong
    }
}
