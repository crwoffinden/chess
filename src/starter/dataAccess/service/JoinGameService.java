package dataAccess.service;

import chess.ChessGame;
import dataAccess.AlreadyTakenException;
import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DataAccessException;
import dataAccess.model.Game;
import dataAccess.request.JoinGameRequest;
import dataAccess.result.JoinGameResult;

/**Allows a user to join a game*/
public class JoinGameService {
    /**Allows the user to join a game
     *
     * @param r
     * @return
     */
    //FIXME this is memory implementation adjust when adding actual database
    public JoinGameResult joinGame(JoinGameRequest r, String authToken, Database db) {
        GameDAO gDAO = db.getGameDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            String username = aDAO.find(authToken).getUsername();
            try {
                gDAO.claimSpot(r.getGameID(), r.getPlayerColor(), username);
                JoinGameResult res = new JoinGameResult(null, true);
                return res;
            } catch (AlreadyTakenException a) {
                JoinGameResult res = new JoinGameResult("Error: already taken", false);
                return res;
            } catch (DataAccessException d) {
                JoinGameResult res = new JoinGameResult("Error: bad request", false);
                return res;
            }
        } catch (DataAccessException d) {
            JoinGameResult res = new JoinGameResult("Error: unauthorized", false);
            return res;
        }
    }
}
