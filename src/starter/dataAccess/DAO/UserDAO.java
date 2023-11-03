package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.User;

import java.util.HashMap;
import java.util.Map;

/**Accesses and updates the user table*/
public class UserDAO {
    /**Map that will store the users*/
    private Map<String, User> users = new HashMap<>();

    /**Adds user to the map
     *
     * @param user
     * @throws DataAccessException
     */
    public void insert(User user) throws DataAccessException {
        try {
            User otherUser = users.get(user.getUsername());
            if (otherUser != null) throw new DataAccessException("Already a user with that username");
        } finally {
            users.put(user.getUsername(), user);
        }
    }

    /**Finds a user by username
     *
     * @param username
     * @return
     * @throws DataAccessException
     */
    public User find(String username) throws DataAccessException {
        User foundUser = users.get(username);
        if (foundUser == null) throw new DataAccessException("No user with that username.");
        return foundUser;
    }

    /**Deletes a user from the table
     *
     * @param user
     * @throws DataAccessException
     */
    public void remove(User user) throws DataAccessException {
        boolean deleted = users.remove(user.getUsername(), user);
        if (!deleted) throw new DataAccessException("User not found.");
    }

    /**Clears the user map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {
        users.clear();
    }
}
