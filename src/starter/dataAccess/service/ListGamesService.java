package dataAccess.service;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import model.Game;
import result.ListGamesResult;

import java.sql.Connection;

/**Lists all of the games*/
public class ListGamesService {
    /**Lists all the games
     *
     * @return
     */
    //FIXME this is for memory implementation adjust when adding the actual database
    public ListGamesResult listGames(String authToken) {
        Database db = new Database();
        try { //Gets all the games and sends a response
            Connection conn = db.getConnection();
            GameDAO gDAO = new GameDAO(conn);
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);

            aDAO.find(authToken);
            try {
                Game[] games = gDAO.findAll();
                db.closeConnection(conn);
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
