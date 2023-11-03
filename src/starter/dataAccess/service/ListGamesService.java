package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import dataAccess.model.Game;
import dataAccess.result.ListGamesResult;

import java.util.Map;

/**Lists all of the games*/
public class ListGamesService {
    /**Lists all the games
     *
     * @return
     */
    //FIXME this is for memory implementation adjust when adding the actual database
    public ListGamesResult listGames(String authToken, Database db) {
        GameDAO gDAO = db.getGameDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            aDAO.find(authToken);
            try {
                Map<Integer, Game> gameMap = gDAO.findAll();
                Game[] games = new Game[gameMap.size()];
                int i = 0;
                for (Game game : gameMap.values()) {
                    games[i] = game;
                    ++i;
                }
                ListGamesResult res = new ListGamesResult(games, null, true);
                return res;
            } catch (Exception e) { //FIXME may be incorrect
                return new ListGamesResult(null, "Error: Internal Server Error", false);
            }
        } catch (DataAccessException d) {
            return new ListGamesResult(null, "Error: unauthorized", false);
        }
    }
}
