package dataAccess.DAO;

import dataAccess.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    /**Map that will store the users*/
    private Map<String, User> users = new HashMap<>();

    /**Adds user to the map
     *
     * @param user
     */
    public void insert(User user) {

    }

    /**Finds a user by username
     *
     * @param username
     * @return
     */
    public User find(String username){
        return null;
    }

    /**Deletes a user from the table
     *
     * @param user
     */
    public void remove(User user) {

    }

    /**Clears the user map
     *
     */
    public void clear(){

    }
}
