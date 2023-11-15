package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**Accesses and updates the user table*/
public class UserDAO {
    /**Connection with the database (must be connected or the data cannot be updated*/
    private final Connection conn;

    /**Constructor
     * @param conn
     */
    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    /**Map that will store the users*/
    //private Map<String, User> users = new HashMap<>();

    /**Adds user to the map
     *
     * @param user
     * @throws DataAccessException
     */
    public void insert(User user) throws DataAccessException {
        //We can structure our string to be similar to a sql command, but if we insert question
        //marks we can change them later with help from the statement
        String sql = "INSERT INTO User (Username, Password, Email) VALUES(?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //Using the statements built-in set(type) functions we can pick the question mark we want
            //to fill in and give it a proper value. The first argument corresponds to the first
            //question mark found in our sql String
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while inserting a user into the database");
        }
    }

    /**Finds a user by username
     *
     * @param username
     * @return
     * @throws DataAccessException
     */
    public User find(String username) throws DataAccessException {
        User foundUser;
        ResultSet rs;
        String sql = "SELECT * FROM User WHERE Username = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                foundUser = new User(rs.getString("Username"), rs.getString("Password"),
                        rs.getString("Email"));
                return foundUser;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while finding a user in the database");
        }
    }

    /**Deletes a user from the table
     *
     * @param user
     * @throws DataAccessException
     */
    public void remove(User user) throws DataAccessException {
        String sql = "DELETE FROM User WHERE Username = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the user table");
        }
    }


    /**Clears the user map
     *
     * @throws DataAccessException
     */
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM User";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the user table");
        }
    }
}
