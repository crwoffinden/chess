package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DataAccessException;
import dataAccess.result.LogoutResult;

/**Logs a user out*/
public class LogoutService {
    /**Logs the user out
     *
     * @return
     */

    //FIXME this is memory implementation adjust when adding the actual database
    public LogoutResult logout(String authtoken, Database db) {
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            aDAO.remove(aDAO.find(authtoken));
            LogoutResult res = new LogoutResult(null, true);
            return res;
        } catch (DataAccessException e) {
            LogoutResult res = new LogoutResult("Error: bad request", false);
            return res;
        }
    }
}
