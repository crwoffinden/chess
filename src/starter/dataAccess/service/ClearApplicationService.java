package dataAccess.service;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import result.ClearApplicationResult;

import java.sql.Connection;

/**Clears all data from the application*/
public class ClearApplicationService {
    /**Clears all the data
     *
     * @return
     */
    //FIXME this is the memory implementation adjust when you add the actual database
    public ClearApplicationResult clear() {
        Database db = new Database();
        try { //Clears the database and sends a success response
            Connection conn = db.getConnection();
            UserDAO uDAO = new UserDAO(conn);
            uDAO.clear();
            GameDAO gDAO = new GameDAO(conn);
            gDAO.clear();
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);
            aDAO.clear();
            db.closeConnection(conn);
            ClearApplicationResult res = new ClearApplicationResult(null, true);
            return res;
        } catch (DataAccessException e) {
            return new ClearApplicationResult("Error: Internal Server Error", false);
        }
        /*UserDAO uDAO = db.getUserDAO();
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
        }*/

    }
}
