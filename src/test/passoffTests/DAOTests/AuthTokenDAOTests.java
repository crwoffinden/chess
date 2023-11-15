package passoffTests.DAOTests;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DataAccessException;
import dataAccess.model.AuthToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTokenDAOTests {
    //Database that we will use for testing
    private Database db;

    //The AuthToken we create for testing
    private AuthToken randomAuthToken;

    private AuthTokenDAO aDAO;

    @BeforeEach
    public void setUp() throws DataAccessException { //Sets up what we are going to use before each test
        db = new Database();
        randomAuthToken = new AuthToken("abc123", "TheifBob");
        Connection conn = db.getConnection();
        aDAO = new AuthTokenDAO(conn);
        aDAO.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException { //Closes the connection to the database file
        //We set the commit to false because we don't want to commit the changes after the tests
        db.closeConnection(db.getConnection());
    }

    @Test
    //Checks to see if the AuthTokenDAO inserts the way we want it to
    public void insertPass() throws DataAccessException {
        //Inserts our created authtoken into the database
        aDAO.insert(randomAuthToken);
        //Checks to see if the authtoken we inserted is in the table
        AuthToken testAuthToken = aDAO.find(randomAuthToken.getAuthToken());
        //Checks to see if the authtoken found by the find function exists
        assertNotNull(testAuthToken);
        //Checks to see if the authtoken found by the find function is our authtoken
        assertEquals(randomAuthToken, testAuthToken);
    }

    @Test
    //Checks to see if the AuthTokenDAO doesn't insert the way we expect it to
    public void insertFail() throws DataAccessException {
        //Inserts our created authtoken into the database
        aDAO.insert(randomAuthToken);
        //Checks if a DataAccessException is thrown when we try to reinsert the same authtoken
        assertThrows(DataAccessException.class, () -> aDAO.insert(randomAuthToken));
    }

    @Test
    //Checks to see if the find method in the AuthTokenDAO works the way we want it to
    public void findPass() throws DataAccessException {
        //Inserts our authtoken into the database
        aDAO.insert(randomAuthToken);
        //Checks to see if the authtoken found by the find method exists
        AuthToken testAuthToken = aDAO.find(randomAuthToken.getAuthToken());
        assertNotNull(testAuthToken);
        //Checks to see if the authtoken found by the find method is the one we expected it to find
        assertEquals(randomAuthToken, testAuthToken);
    }

    @Test
    //Checks to see if the find method in the AuthTokenDAO fails the way we expect it to
    public void findFail() {
        //We tell the AuthTokenDAO to find an authtoken that isn't in the table.
        assertThrows(DataAccessException.class, () -> aDAO.find(randomAuthToken.getAuthToken()));
    }

    @Test
    //Checks to see if the remove method works the way it's supposed to
    public void removeTestPass() throws DataAccessException {
        //Insert testAuthToken into the database
        aDAO.insert(randomAuthToken);
        //Ensures that our testAuthToken is in the database to begin with
        AuthToken testAuthToken = aDAO.find(randomAuthToken.getAuthToken());
        assertNotNull(testAuthToken);
        assertEquals(randomAuthToken, testAuthToken);
        //Tells the testAuthTokenDAO to delete this testAuthToken
        aDAO.remove(randomAuthToken);
        //Checks that the testAuthToken with the deleted associated username is gone
        assertThrows(DataAccessException.class, () -> aDAO.find(randomAuthToken.getAuthToken()));
    }

    @Test
    //Checks to see if the remove method fails the way it's supposed to
    public void removeTestFail() throws DataAccessException {
        //Insert authtoken into the database
        aDAO.insert(randomAuthToken);
        //Ensures that our authtoken is in the database to begin with
        AuthToken testAuthToken = aDAO.find(randomAuthToken.getAuthToken());
        assertNotNull(testAuthToken);
        assertEquals(randomAuthToken, testAuthToken);
        //Create additional authtoken to ensure test works properly
        AuthToken testAuthToken2 = new AuthToken(randomAuthToken.getAuthToken(), "Gale");
        //Tells the testAuthTokenDAO to remove an AuthToken that is not in the database
        aDAO.remove(testAuthToken2);
        assertNotNull(aDAO.find(randomAuthToken.getAuthToken()));
    }

    @Test
    //Checks to see if the clear method in the AuthTokenDAO works properly
    public void clearTest1() throws DataAccessException {
        //We first insert our authtoken into the table and ensure that the authtoken was in the table originally
        aDAO.insert(randomAuthToken);
        AuthToken testAuthToken = aDAO.find(randomAuthToken.getAuthToken());
        assertNotNull(testAuthToken);
        assertEquals(randomAuthToken, testAuthToken);
        //Then we tell the AuthTokenDAO to clear the table, and look for the same authtoken, which should no longer be there
        aDAO.clear();
        assertThrows(DataAccessException.class, () -> aDAO.find(randomAuthToken.getAuthToken()));
    }

    @Test
    //Checks the clear method in the AuthTokenDAO by clearing 2 authtokens
    public void clearTest2() throws DataAccessException {
        //New authtoken to add to the DAO
        AuthToken authtoken2 = new AuthToken("Jim", "Jim");
        //We first insert our authtokens into the table and ensure that the authtokens were in the table originally
        aDAO.insert(randomAuthToken);
        aDAO.insert(authtoken2);
        AuthToken testAuthToken1 = aDAO.find(randomAuthToken.getAuthToken());
        assertNotNull(testAuthToken1);
        assertEquals(randomAuthToken, testAuthToken1);
        AuthToken testAuthToken2 = aDAO.find(authtoken2.getAuthToken());
        assertNotNull(testAuthToken2);
        assertEquals(authtoken2, testAuthToken2);
        //Then we tell the AuthTokenDAO to clear the table, and look for the same authtokens, which should no longer be there
        aDAO.clear();
        assertThrows(DataAccessException.class, () -> aDAO.find(randomAuthToken.getAuthToken()));
        assertThrows(DataAccessException.class, () -> aDAO.find(authtoken2.getAuthToken()));
    }
}
