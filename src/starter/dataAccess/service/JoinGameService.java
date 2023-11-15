package dataAccess.service;

import dataAccess.AlreadyTakenException;
import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import dataAccess.request.JoinGameRequest;
import dataAccess.result.ClearApplicationResult;
import dataAccess.result.JoinGameResult;

import java.sql.Connection;

/**Allows a user to join a game*/
public class JoinGameService {
    /**Allows the user to join a game
     *
     * @param r
     * @return
     */
    //FIXME this is memory implementation adjust when adding actual database
    public JoinGameResult joinGame(JoinGameRequest r, String authToken) {
        Database db = new Database();
        try { //Adds a user to a game and sends a response
            Connection conn = db.getConnection();
            GameDAO gDAO = new GameDAO(conn);
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);
            String username = aDAO.find(authToken).getUsername();
            try {
                gDAO.claimSpot(r.getGameID(), r.getPlayerColor(), username);
                db.closeConnection(conn);
                JoinGameResult res = new JoinGameResult(null, true);
                return res;
            } catch (DataAccessException d) {
                JoinGameResult res = new JoinGameResult("Error: bad request", false);
                return res;
            } catch (AlreadyTakenException a) {
                JoinGameResult res = new JoinGameResult("Error: already taken", false);
                return res;
            }
        } catch (DataAccessException d) {
            JoinGameResult res = new JoinGameResult("Error: unauthorized", false);
            return res;
        }
    }
}
