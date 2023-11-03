package dataAccess.DAO;

import chess.ChessGame;
import dataAccess.AlreadyTakenException;
import dataAccess.DataAccessException;
import dataAccess.model.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**Accesses and updates the game table*/
public class GameDAO {
    /**Map that will store the games*/
    private Map<Integer, Game> games = new HashMap<>();
    private int id = 0;

    /**Adds game to the map
     *
     * @param game
     * @throws DataAccessException
     */
    public void insert(Game game) throws DataAccessException {
        try {
            Game otherGame = games.get(game.getGameID());
            if (otherGame != null) throw new DataAccessException("Already a game with that ID");
        } finally {
            games.put(game.getGameID(), game);
        }
    }

    /**Finds a game by gameID
     *
     * @param gameID
     * @return
     * @throws DataAccessException
     */
    public Game find(int gameID) throws  DataAccessException {
        Game foundGame = games.get(gameID);
        if (foundGame == null) throw new DataAccessException("No games with that ID.");
        return foundGame;
    }

    public Map<Integer, Game> findAll() {
        return games;
    }

    public void claimSpot(int gameID, ChessGame.TeamColor color, String username)
            throws AlreadyTakenException, DataAccessException {
        Game game = games.get(gameID);
        if (game == null) throw new DataAccessException("Game not found");
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.getWhiteUsername() != null) throw new AlreadyTakenException("Already taken");
            else {
                game.setWhiteUsername(username);
                games.put(gameID, game);
            }
        }
        else if (color == ChessGame.TeamColor.BLACK) {
            if (game.getBlackUsername() != null) throw new AlreadyTakenException("Already taken");
            else {
                game.setBlackUsername(username);
                games.put(gameID, game);
            }
        }
    }

    public void update(Game game) {
        games.put(game.getGameID(), game);
    }

    /**Deletes a game from the table
     *
     * @param game
     * @throws DataAccessException
     */
    public void remove(Game game) throws DataAccessException {
        boolean deleted = games.remove(game.getGameID(), game);
        if (!deleted) throw new DataAccessException("Game not found.");
    }

    /**Clears the game map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {
        games.clear();
    }

    //FIXME this is for memory implementation, may discard entirely when adding actual database
    public int getNewID() {
        int newID;
        Random r = new Random();
        do {
            newID = r.nextInt(10000000);
        } while (games.get(newID) != null);
        return newID;
    }
}
