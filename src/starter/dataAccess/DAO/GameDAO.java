package dataAccess.DAO;

import dataAccess.model.Game;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    /**Map that will store the games*/
    private Map<Integer, Game> games = new HashMap<>();

    /**Adds game to the map
     *
     * @param game
     */
    public void insert(Game game) {

    }

    /**Finds a game by gameID
     *
     * @param gameID
     * @return
     */
    public Game find(int gameID){
        return null;
    }

    /**Deletes a game from the table
     *
     * @param game
     */
    public void remove(Game game) {

    }

    /**Clears the game map
     *
     */
    public void clear(){

    }
}
