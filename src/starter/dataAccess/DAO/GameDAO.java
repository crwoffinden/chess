package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.Game;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    /**Map that will store the games*/
    private Map<Integer, Game> games = new HashMap<>();

    /**Adds game to the map
     *
     * @param game
     * @throws DataAccessException
     */
    public void insert(Game game) throws DataAccessException {

    }

    /**Finds a game by gameID
     *
     * @param gameID
     * @return
     * @throws DataAccessException
     */
    public Game find(int gameID) throws  DataAccessException {
        return null;
    }

    /**Deletes a game from the table
     *
     * @param game
     * @throws DataAccessException
     */
    public void remove(Game game) throws DataAccessException {

    }

    /**Clears the game map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {

    }
}
