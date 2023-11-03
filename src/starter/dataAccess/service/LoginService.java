package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.UserDAO;
import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import dataAccess.model.User;
import dataAccess.request.LoginRequest;
import dataAccess.result.LoginResult;

import java.util.UUID;

/**Logs a user in*/
public class LoginService {
    /**Log a user in
     *
     * @param r
     * @return
     */
    //FIXME this is for memory implementation adjust when adding the actual database
    public LoginResult login(LoginRequest r, Database db) {
        UserDAO uDAO = db.getUserDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            User foundUser = uDAO.find(r.getUsername());
            if (foundUser.getUsername().equals(r.getUsername()) && foundUser.getPassword().equals(r.getPassword())) {
                String authToken = UUID.randomUUID().toString();
                aDAO.insert(new AuthToken(authToken, r.getUsername()));
                LoginResult res = new LoginResult(r.getUsername(), authToken, null, true);
                return res;
            } else {
                return new LoginResult(null, null, "Error: unauthorized", false);
            }
        } catch (DataAccessException e) {
            return new LoginResult(null, null, "Error: unauthorized", false);
        }
    }
}
