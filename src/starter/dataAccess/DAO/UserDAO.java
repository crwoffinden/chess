package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    /**Map that will store the users*/
    private Map<String, User> users = new HashMap<>();

    /**Adds user to the map
     *
     * @param user
     * @throws DataAccessException
     */
    public void insert(User user) throws DataAccessException {

    }

    /**Finds a user by username
     *
     * @param username
     * @return
     * @throws DataAccessException
     */
    public User find(String username) throws DataAccessException {
        return null;
    }

    /**Deletes a user from the table
     *
     * @param user
     * @throws DataAccessException
     */
    public void remove(User user) throws DataAccessException {

    }

    /**Clears the user map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {

    }
}
