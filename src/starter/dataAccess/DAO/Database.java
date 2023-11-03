package dataAccess.DAO;

import dataAccess.model.AuthToken;
import dataAccess.model.Game;
import dataAccess.model.User;

import java.util.HashMap;
import java.util.Map;

//FIXME This is the memory implementation you will need to fix this when you add the real database
public class Database {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;

    public Database(UserDAO uDAO, GameDAO gDAO, AuthTokenDAO aDAO) {
        userDAO = uDAO;
        gameDAO = gDAO;
        authTokenDAO = aDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public AuthTokenDAO getAuthTokenDAO() {
        return authTokenDAO;
    }
}
