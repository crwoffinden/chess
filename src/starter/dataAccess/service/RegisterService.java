package dataAccess.service;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import dataAccess.model.User;
import dataAccess.request.RegisterRequest;
import dataAccess.result.RegisterResult;

import java.sql.Connection;
import java.util.UUID;

/**Registers a new user*/
public class RegisterService {
    /**Registers a new user
     *
     * @param r
     * @return
     */
    //FIXME this is for memory implementation adjust when adding actual database
    public RegisterResult register(RegisterRequest r) {
        Database db = new Database();
        try { //Registers a user and sends a response
            Connection conn = db.getConnection();
            UserDAO uDAO = new UserDAO(conn);
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);

            User newUser = new User(r.getUsername(), r.getPassword(), r.getEmail());
            if (newUser.getPassword() == null) {
                return new RegisterResult(null, null, "Error: bad request", false);
            }

            uDAO.insert(newUser);
            String authToken = UUID.randomUUID().toString();
            aDAO.insert(new AuthToken(authToken, r.getUsername()));
            db.closeConnection(conn);
            RegisterResult res = new RegisterResult(r.getUsername(), authToken, null, true);
            return res;
        } catch (DataAccessException e) {
            return new RegisterResult(null, null, "Error: already taken", false);
        }
    }
}
