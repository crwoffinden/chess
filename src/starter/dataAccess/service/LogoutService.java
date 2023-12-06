package dataAccess.service;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import result.LogoutResult;

import java.sql.Connection;

/**Logs a user out*/
public class LogoutService {
    /**Logs the user out
     *
     * @return
     */

    //FIXME this is memory implementation adjust when adding the actual database
    public LogoutResult logout(String authtoken) {
        Database db = new Database();
        try { //Logs a user out and sends a response
            Connection conn = db.getConnection();
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);

            aDAO.remove(aDAO.find(authtoken));
            db.closeConnection(conn);
            LogoutResult res = new LogoutResult(null, true);
            return res;
        } catch (DataAccessException e) {
            LogoutResult res = new LogoutResult("Error: bad request", false);
            return res;
        }
    }
}
