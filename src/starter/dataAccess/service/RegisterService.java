package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.UserDAO;
import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import dataAccess.model.User;
import dataAccess.request.RegisterRequest;
import dataAccess.result.RegisterResult;

import java.util.UUID;

/**Registers a new user*/
public class RegisterService {
    /**Registers a new user
     *
     * @param r
     * @return
     */
    //FIXME this is for memory implementation adjust when adding actual database
    public RegisterResult register(RegisterRequest r, Database db) {
        UserDAO uDAO = db.getUserDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        User newUser = new User(r.getUsername(), r.getPassword(), r.getEmail());
        if (newUser.getPassword() == null) {
            return new RegisterResult(null, null, "Error: bad request", false);
        }
        try {
            uDAO.insert(newUser);
            String authToken = UUID.randomUUID().toString();
            aDAO.insert(new AuthToken(authToken, r.getUsername()));
            RegisterResult res = new RegisterResult(r.getUsername(), authToken, null, true);
            return res;
        } catch (DataAccessException e) {
            return new RegisterResult(null, null, "Error: already taken", false);
        }
    }
}
