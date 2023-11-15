package dataAccess.DAO;

import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**Accesses and updates the authtoken table*/
public class AuthTokenDAO {
    /**Connection with the database (must be connected or the data cannot be updated*/
    private final Connection conn;

    /**Constructor
     * @param conn
     */
    public AuthTokenDAO(Connection conn) {
        this.conn = conn;
    }

    /**Adds an authtoken to the table
     * @param authtoken
     */
    public void insert(AuthToken authtoken) throws DataAccessException {
        //We can structure our string to be similar to a sql command, but if we insert question
        //marks we can change them later with help from the statement
        String sql = "INSERT INTO AuthToken (AuthToken, Username) VALUES(?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            //Using the statements built-in set(type) functions we can pick the question mark we want
            //to fill in and give it a proper value. The first argument corresponds to the first
            //question mark found in our sql String
            stmt.setString(1, authtoken.getAuthToken());
            stmt.setString(2, authtoken.getUsername());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while inserting an authtoken into the database");
        }
    }

    /**Finds an authtoken based on authtoken
     * @param authtoken
     * @return foundAuthtoken
     */
    public AuthToken find(String authtoken) throws DataAccessException { //FIXME username might be more useful
        AuthToken foundAuthtoken;
        ResultSet rs;
        String sql = "SELECT * FROM AuthToken WHERE AuthToken = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authtoken);
            rs = stmt.executeQuery();
            if (rs.next()) {
                foundAuthtoken = new AuthToken(rs.getString("AuthToken"), rs.getString("Username"));
                return foundAuthtoken;
            } else {
                throw new DataAccessException("Error: bad request");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while finding an authtoken in the database");
        }
    }

    /**Clears the authtoken table*/
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM AuthToken";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the authtoken table");
        }
    }

    public void remove(AuthToken authToken) throws DataAccessException {
        String sql = "DELETE FROM AuthToken WHERE Username = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error encountered while clearing the authtoken table");
        }
    }
}
