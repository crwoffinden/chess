package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DAO.UserDAO;
import dataAccess.DataAccessException;
import dataAccess.result.ClearApplicationResult;

/**Clears all data from the application*/
public class ClearApplicationService {
    /**Clears all the data
     *
     * @return
     */
    //FIXME this is the memory implementation adjust when you add the actual database
    public ClearApplicationResult clear(Database db) {
        UserDAO uDAO = db.getUserDAO();
        GameDAO gDAO = db.getGameDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            uDAO.clear();
            gDAO.clear();
            aDAO.clear();
            ClearApplicationResult res = new ClearApplicationResult(null, true);
            return res;
        } catch (DataAccessException e) {
            return new ClearApplicationResult("Error: Internal Server Error", false);
        }

    }
}
