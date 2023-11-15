package dataAccess.DAO;

//FIXME This is the memory implementation you will need to fix this when you add the real database
public class MemoryDatabase {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;

    public MemoryDatabase(UserDAO uDAO, GameDAO gDAO, AuthTokenDAO aDAO) {
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
