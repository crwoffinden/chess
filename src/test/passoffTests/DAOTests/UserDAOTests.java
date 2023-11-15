package passoffTests.DAOTests;

import dataAccess.DAO.Database;
import dataAccess.DAO.UserDAO;
import dataAccess.DataAccessException;
import dataAccess.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests {
    //Database that we will use for testing
    private Database db;

    //The User we create for testing
    private User randomUser;

    private UserDAO uDAO;

    @BeforeEach
    public void setUp() throws DataAccessException { //Sets up what we are going to use before each test
        db = new Database();
        randomUser = new User("TheifBob", "abc123", "TheifBob@email.com");
        Connection conn = db.getConnection();
        uDAO = new UserDAO(conn);
        uDAO.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException { //Closes the connection to the database file
        //We set the commit to false because we don't want to commit the changes after the tests
        db.closeConnection(db.getConnection());
    }

    @Test
    //Checks to see if the UserDAO inserts the way we want it to
    public void insertPass() throws DataAccessException {
        //Inserts our created User into the database
        uDAO.insert(randomUser);
        //Checks to see if the user we inserted is in the table
        User testUser = uDAO.find(randomUser.getUsername());
        //Checks to see if the user found by the find function exists
        assertNotNull(testUser);
        //Checks to see if the user found by the find function is our user
        assertEquals(randomUser, testUser);
    }

    @Test
    public void insertFail() throws DataAccessException { //Checks to see if the UserDAO doesn't insert the way we expect it to
        //Inserts our created User into the database
        uDAO.insert(randomUser);
        //Checks if a DataAccessException is thrown when we try to reinsert the same user
        assertThrows(DataAccessException.class, () -> uDAO.insert(randomUser));
    }

    @Test
    public void findPass() throws DataAccessException { //Checks to see if the find method in the UserDAO works the way we want it to
        //Inserts our user into the database
        uDAO.insert(randomUser);
        //Checks to see if the user found by the find method exists
        User testUser = uDAO.find(randomUser.getUsername());
        assertNotNull(testUser);
        //Checks to see if the user found by the find method is the one we expected it to find
        assertEquals(randomUser, testUser);
    }

    @Test
    public void findFail() throws DataAccessException { //Checks to see if the find method in the UserDAO fails the way we expect it to
        //We tell the UserDAO to find a user that isn't in the table.
        User testUser = uDAO.find(randomUser.getUsername());
        assertNull(testUser);
    }

    @Test
    //Checks to see if the remove method works the way it's supposed to
    public void removeTestPass() throws DataAccessException {
        //Insert User into the database
        uDAO.insert(randomUser);
        //Ensures that our User is in the database to begin with
        User testUser = uDAO.find(randomUser.getUsername());
        assertNotNull(testUser);
        assertEquals(randomUser, testUser);
        //Tells the testUserDAO to delete this User
        uDAO.remove(randomUser);
        //Checks that the User with the deleted associated username is gone
        assertNull(uDAO.find(randomUser.getUsername()));
    }

    @Test
    //Checks to see if the remove method fails the way it's supposed to
    public void removeTestFail() throws DataAccessException {
        //Insert user into the database
        uDAO.insert(randomUser);
        //Ensures that our user is in the database to begin with
        User testUser = uDAO.find(randomUser.getUsername());
        assertNotNull(testUser);
        assertEquals(randomUser, testUser);
        //Create additional user to ensure test works properly
        User testUser2 = new User("PodcastGuy", "Gale", "email.adress");
        //Tells the UserDAO to remove a user that is not in the database
        uDAO.remove(testUser2);
        assertNotNull(uDAO.find(randomUser.getUsername()));
    }

    @Test
    public void clearTest1() throws DataAccessException { //Checks to see if the clear method in the UserDAO works properly
        //We first insert our user into the table and ensure that the user was in the table originally
        uDAO.insert(randomUser);
        User testUser = uDAO.find(randomUser.getUsername());
        assertNotNull(testUser);
        assertEquals(randomUser, testUser);
        //Then we tell the UserDAO to clear the table, and look for the same user, which should no longer be there
        uDAO.clear();
        testUser = uDAO.find(randomUser.getUsername());
        assertNull(testUser);
    }

    @Test
    //Checks the clear method in the UserDAO by clearing 2 users
    public void clearTest2() throws DataAccessException {
        //New user to add to the DAO
        User user2 = new User("Jim", "Jim", "Jim");
        //We first insert our users into the table and ensure that the users were in the table originally
        uDAO.insert(randomUser);
        uDAO.insert(user2);
        User testUser1 = uDAO.find(randomUser.getUsername());
        assertNotNull(testUser1);
        assertEquals(randomUser, testUser1);
        User testUser2 = uDAO.find(user2.getUsername());
        assertNotNull(testUser2);
        assertEquals(user2, testUser2);
        //Then we tell the UserDAO to clear the table, and look for the same users, which should no longer be there
        uDAO.clear();
        testUser1 = uDAO.find(randomUser.getUsername());
        assertNull(testUser1);
        testUser2 = uDAO.find(user2.getUsername());
        assertNull(testUser2);
    }
}

