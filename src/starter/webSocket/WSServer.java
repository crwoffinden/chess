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
        GameDAO gDAO = new GameDAO(conn);
        AuthTokenDAO aDAO = new AuthTokenDAO(conn);
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            UserGameCommand.CommandType type = command.getCommandType();
            String authtoken = command.getAuthString();
            AuthToken a = aDAO.find(authtoken);
            if (a != null) {
                String username = a.getUsername();
                switch (type) {
                    case JOIN_PLAYER:
                        JoinPlayerUserGameCommand joinCommand =
                                gson.fromJson(message, JoinPlayerUserGameCommand.class);
                        int gameID = joinCommand.getGameID();
                        Game g = gDAO.find(gameID);
                        if (g != null) {
                            webSocket.Connection newConn = new webSocket.Connection(gameID, username, session);
                            if (!connections.connections.contains(newConn)) {
                                connections.add(gameID, username, session);
                            }
                            if ((joinCommand.getPlayerColor().equals(ChessGame.TeamColor.WHITE)
                                    && g.getWhiteUsername().equals(username))
                                    || (joinCommand.getPlayerColor().equals(ChessGame.TeamColor.BLACK)
                                    && g.getBlackUsername().equals(username))) {
                                if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                                ChessGame.TeamColor color = joinCommand.getPlayerColor();
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
                        if (g != null) {
                            webSocket.Connection newConn = new webSocket.Connection(gameID, username, session);
                            if (!connections.connections.contains(newConn)) {
                                connections.add(gameID, username, session);
                            }
                            if (!gameOver.containsKey(gameID)) gameOver.put(gameID, false);
                            ChessGame game = g.getGame();
                            LoadGameServerMessage loadMessage =
                                    new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                            String json = gson.toJson(loadMessage);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.ONLY_USER, json);
                            String notificationString = username + " has joined the game as an observer";
                            NotificationServerMessage notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            json = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                        } else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                    case MAKE_MOVE:
                        MakeMoveUserGameCommand moveCommand = gson.fromJson(message, MakeMoveUserGameCommand.class);
                        gameID = ((MakeMoveUserGameCommand) command).getGameID();
                        if (gDAO.find(gameID) != null) {
                            if (!gameOver.get(gameID)) {
                                ChessGame game = gDAO.find(gameID).getGame();
                                ChessMove move = moveCommand.getMove();
                                if (game.validMoves(move.getStartPosition()).contains(move)) {
                                    game.makeMove(move);
                                    gDAO.update(gameID, game);
                                    LoadGameServerMessage loadMessage =
                                            new LoadGameServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
                                    String json = gson.toJson(loadMessage);
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
                                    String notificationString = username + " moved " + pieceType + startRow + startCol + endRow + endCol;
                                    NotificationServerMessage notification =
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
                                    } else if (game.isInStalemate(game.getTeamTurn())) {
                                        notificationString = "Stalemate. It's a draw.";
                                        notification =
                                                new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                                        json = gson.toJson(notification);
                                        connections.broadcast(gameID, username, ConnectionManager.Recipients.EVERYONE, json);
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
                        if (gDAO.find(gameID) != null) {
                            connections.remove(username);
                            if (gDAO.find(gameID).getWhiteUsername().equals(username)) {
                                gDAO.claimSpot(gameID, ChessGame.TeamColor.WHITE, null);
                            } else if (gDAO.find(gameID).getBlackUsername().equals(username)) {
                                gDAO.claimSpot(gameID, ChessGame.TeamColor.BLACK, null);
                            }
                            String notificationString = username + "left the game.";
                            NotificationServerMessage notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            String json = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                        }
                        else {
                            ErrorServerMessage errorMessage =
                                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Invalid gameID. Try again.");
                            String json = gson.toJson(errorMessage);
                            session.getRemote().sendString(json);
                        }
                        break;
                    case RESIGN:
                        ResignUserGameCommand resignCommand = gson.fromJson(message, ResignUserGameCommand.class);
                        gameID = resignCommand.getGameID();
                        if (gDAO.find(gameID) != null) {
                            String opponentUsername = gDAO.find(gameID).getWhiteUsername();
                            if (opponentUsername.equals(username))
                                opponentUsername = gDAO.find(gameID).getBlackUsername();
                            String notificationString = username + " resigned. " + opponentUsername + " wins!";
                            NotificationServerMessage notification =
                                    new NotificationServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationString);
                            String json = gson.toJson(notification);
                            connections.broadcast(gameID, username, ConnectionManager.Recipients.NOT_USER, json);
                            gameOver.remove(gameID);
                            gameOver.put(gameID, true);
                        }
                        else {
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
                    new ErrorServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: Try again.");
            String json = gson.toJson(errorMessage);
            db.closeConnection(conn);
            session.getRemote().sendString(json);
        }
        db.closeConnection(conn);
    }
}
