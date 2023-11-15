package dataAccess.DAO;

import chess.*;
import com.google.gson.*;
import dataAccess.AlreadyTakenException;
import dataAccess.DataAccessException;
import dataAccess.model.Game;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**Accesses and updates the game table*/
public class GameDAO {
    /**Connection with the database (must be connected or the data cannot be updated*/
    private final Connection conn;

    class PositionAdapter implements JsonDeserializer<ChessPosition> {

        @Override
        public ChessPosition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Position.class);
        }
    }

    class MoveAdapter implements JsonDeserializer<ChessMove> {

        @Override
        public ChessMove deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Move.class);
        }
    }

    class PieceAdapter implements JsonDeserializer<ChessPiece> {

        @Override
        public ChessPiece deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Piece.class);
        }
    }

    class BoardAdapter implements JsonDeserializer<ChessBoard> {

        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Board.class);
        }
    }
    class GameAdapter implements JsonDeserializer<ChessGame> {

        @Override
        public ChessGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Game.class);
        }
    }

    private GsonBuilder builder = new GsonBuilder();
    /**Constructor
     * @param conn
     */
    public GameDAO(Connection conn) {
        this.conn = conn;
        builder.registerTypeAdapter(ChessPosition.class, new PositionAdapter());
        builder.registerTypeAdapter(ChessMove.class, new MoveAdapter());
        builder.registerTypeAdapter(ChessPiece.class, new PieceAdapter());
        builder.registerTypeAdapter(ChessBoard.class, new BoardAdapter());
        builder.registerTypeAdapter(ChessGame.class, new GameAdapter());


    }
    /**Map that will store the games*/

    public void insert(Game game) throws DataAccessException {
        //We can structure our string to be similar to a sql command, but if we insert question
        //marks we can change them later with help from the statement
        String sql = "INSERT INTO Game (GameID, WhiteUsername, BlackUsername, GameName, Game) VALUES(?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //Using the statements built-in set(type) functions we can pick the question mark we want
            //to fill in and give it a proper value. The first argument corresponds to the first
            //question mark found in our sql String
            stmt.setInt(1, game.getGameID());
            stmt.setString(2, game.getWhiteUsername());
            stmt.setString(3, game.getBlackUsername());
            stmt.setString(4, game.getGameName());
            var json = new Gson().toJson(game.getGame());
            stmt.setString(5, json);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while inserting a game into the database");
        }
    }

    /**Finds a game by gameID
     *
     * @param gameID
     * @return
     * @throws DataAccessException
     */
    public Game find(int gameID) throws  DataAccessException {
        Game foundGame;
        ResultSet rs;
        String sql = "SELECT * FROM Game WHERE GameID = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            rs = stmt.executeQuery();
            if (rs.next()) {

                var json = rs.getString("Game");

                ChessGame gameState = builder.create().fromJson(json, ChessGame.class);
                foundGame = new Game(rs.getInt("GameID"), rs.getString("WhiteUsername"),
                        rs.getString("BlackUsername"), rs.getString("GameName"),
                        gameState);
                return foundGame;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while finding a game in the database");
        }
    }

    public Game[] findAll() throws DataAccessException {
        ResultSet rs;
        String sql = "SELECT * FROM Game;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            rs = stmt.executeQuery();
            if (rs != null) {
                int size = 0;
                List<Game> gamesList = new ArrayList<>();
                while (rs.next()) {
                    game.Game gameState = (game.Game) builder.create().fromJson(rs.getString("Game"), ChessGame.class);
                    Game game = new Game(rs.getInt("GameID"),
                            rs.getString("WhiteUsername"), rs.getString("BlackUsername"),
                            rs.getString("GameName"), gameState);
                    gamesList.add(game);
                    ++size;
                }
                Game[] gameArray = new Game[size];
                return gamesList.toArray(gameArray);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while finding games in the database");
        }
        //return games;
    }

    public void claimSpot(int gameID, ChessGame.TeamColor color, String username)
            throws AlreadyTakenException, DataAccessException {
        ResultSet rs;
        String sql = "SELECT * FROM Game WHERE GameID = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            rs = stmt.executeQuery();
            if (rs.next()) {
                if (color == ChessGame.TeamColor.WHITE) {
                    if (rs.getString("WhiteUsername") != null) {
                        throw new AlreadyTakenException("Already taken");
                    }
                    else {
                        sql = "UPDATE Game SET WhiteUsername = ? WHERE GameID = ?;";
                        PreparedStatement update = conn.prepareStatement(sql);
                        update.setString(1, username);
                        update.setInt(2, gameID);
                        update.executeUpdate();
                    }
                }
                else if (color == ChessGame.TeamColor.BLACK) {
                    if (rs.getString("BlackUsername") != null) {
                        throw new AlreadyTakenException("Already taken");
                    }
                    else {
                        sql = "UPDATE Game SET BlackUsername = ? WHERE GameID = ?;";
                        PreparedStatement update = conn.prepareStatement(sql);
                        update.setString(1, username);
                        update.setInt(2, gameID);
                        update.executeUpdate();
                    }
                }
            }
            else throw new DataAccessException("Error: bad request");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while updating a game in the database");
        }
    }

    public void update(int gameID, ChessGame game) throws DataAccessException {
        String json = new Gson().toJson(game);
        String sql = "UPDATE Game SET Game = ? WHERE GameID = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, json);
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while updating a game in the database");
        }
    }

    /**Deletes a game from the table
     *
     * @param game
     * @throws DataAccessException
     */
    public void remove(Game game) throws DataAccessException {
        String sql = "DELETE FROM Game WHERE GameID = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, game.getGameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the game table");
        }
    }

    /**Clears the game map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM Game";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the game table");
        }
        //games.clear();
    }

    //FIXME this is for memory implementation, may discard entirely when adding actual database
    public int getNewID() {
        int newID = 0;
        Random r = new Random();
        boolean notUsed = false;
        while (!notUsed) {
            newID = r.nextInt(10000000);
            ResultSet rs;
            String sql = "SELECT * FROM Game WHERE GameID = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newID);
                rs = stmt.executeQuery();
                if (!(rs.next())) notUsed = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return newID;
    }
}
