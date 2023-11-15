package passoffTests.DAOTests;

import chess.ChessBoard;
import chess.ChessGame;
import chess.InvalidMoveException;
import dataAccess.AlreadyTakenException;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DataAccessException;
import dataAccess.model.Game;
import game.Move;
import game.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameDAOTests {
    private Database db;
    private Game bestGame;
    private GameDAO gDao;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Here we can set up any classes or variables we will need for each test
        // lets create a new instance of the Database class
        db = new Database();
        ChessGame game = new game.Game();
        ChessBoard board = game.getBoard();
        board.resetBoard();
        game.setBoard(board);
        // and a new game with best data
        bestGame = new Game(12345, "ThiefBob", "Joe_Schmo",
                "OurGame", game);

        // Here, we'll open the connection in preparation for the test case to use it
        Connection conn = db.getConnection();
        //Then we pass that connection to the GameDAO, so it can access the database.
        gDao = new GameDAO(conn);
        //Let's clear the database as well so any lingering data doesn't affect our tests
        gDao.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        // Here we close the connection to the database file, so it can be opened again later.
        // We will set commit to false because we do not want to save the changes to the database
        // between test cases.
        db.closeConnection(db.getConnection());
    }

    @Test
    public void insertPass() throws DataAccessException {
        // Start by inserting a game into the database.
        gDao.insert(bestGame);
        // Let's use a find method to get the game that we just put in back out.
        Game compareTest = gDao.find(bestGame.getGameID());
        // First lets see if our find method found anything at all. If it did then we know that we got
        // something back from our database.
        assertNotNull(compareTest);
        // Now lets make sure that what we put in is the same as what we got out. If this
        // passes then we know that our insert did put something in, and that it didn't change the
        // data in any way.
        // This assertion works by calling the equals method in the Game class.
        assertEquals(bestGame, compareTest);
    }

    @Test
    public void insertFail() throws DataAccessException {
        // Let's do this test again, but this time lets try to make it fail.
        // If we call the method the first time the game will be inserted successfully.
        gDao.insert(bestGame);

        // However, our sql table is set up so that the column "gameID" must be unique, so trying to insert
        // the same game again will cause the insert method to throw an exception, and we can verify this
        // behavior by using the assertThrows assertion as shown below.

        // Note: This call uses a lambda function. A lambda function runs the code that comes after
        // the "()->", and the assertThrows assertion expects the code that ran to throw an
        // instance of the class in the first parameter, which in this case is a DataAccessException.
        assertThrows(DataAccessException.class, () -> gDao.insert(bestGame));
    }

    @Test
    //Checks to see if the find method in the gameDAO works the way we want it to
    public void findPass() throws DataAccessException {
        //Inserts our game into the database
        gDao.insert(bestGame);
        //Checks to see if the game found by the find method exists
        Game testGame = gDao.find(bestGame.getGameID());
        assertNotNull(testGame);
        //Checks to see if the game found by the find method is the one we expected it to find
        assertEquals(bestGame, testGame);
    }

    @Test
    //Checks to see if the find method in the gameDAO fails the way we expect it to
    public void findFail() throws DataAccessException {
        //We tell the gameDAO to find an game that isn't in the table.
        Game testGame = gDao.find(bestGame.getGameID());
        assertNull(testGame);
    }

    @Test
    //Checks to see if the findAll method in the gameDAO works properly
    public void findAllPass() throws DataAccessException {
        //Create additional games to ensure test works
        Game game2 = new Game(200, "Gale", "TnT", "All4One", new game.Game());
        Game game3 = new Game(1984,"Bob", "Bill", "GoCougs", new game.Game());
        //Insert games into the database
        gDao.insert(bestGame);
        gDao.insert(game2);
        gDao.insert(game3);
        //Tell the gameDAO to find all games
        Game[] testGames = gDao.findAll();
        //Checks to see if the method actually found games
        assertNotNull(testGames);
        //Checks to see if the method found the number of games we expect it to
        assertEquals(3, testGames.length);
        //Checks to see if the games the method found are the ones we expect
        boolean found = false;
        for (int i = 0; i < testGames.length; ++i) {
            if (testGames[i].equals(bestGame)) found = true;
        }
        assertTrue(found);
        found = false;
        for (int i = 0; i < testGames.length; ++i) {
            if (testGames[i].equals(game2)) found = true;
        }
        assertTrue(found);
        found = false;
        for (int i = 0; i < testGames.length; ++i) {
            if (testGames[i].equals(game3)) found = true;
        }
        assertTrue(found);
    }

    @Test
    //Checks to see if the findAll method fails the way we expect it to
    public void findAllFail() throws DataAccessException {
        //Asks the gameDAO to find games that aren't in the database
        assertEquals(0, gDao.findAll().length);
    }


    @Test
    //Checks to see if the clear method in the gameDAO works properly
    public void clearTest() throws DataAccessException {
        //We first insert our game into the table and ensure that the game was in the table originally
        gDao.insert(bestGame);
        Game testGame = gDao.find(bestGame.getGameID());
        assertNotNull(testGame);
        assertEquals(bestGame, testGame);
        //Then we tell the gameDAO to clear the table, and look for the same game, which should no longer be there
        gDao.clear();
        testGame = gDao.find(bestGame.getGameID());
        assertNull(testGame);
    }

    @Test
    //Checks the clear method in the gameDAO by clearing 2 games
    public void clearTest2() throws DataAccessException {
        //New game to add to the DAO
        Game game2 = new Game(200, "Gale", "TnT", "All4One", new game.Game());
        //We first insert our games into the table and ensure that the games were in the table originally
        gDao.insert(bestGame);
        gDao.insert(game2);
        Game testGame1 = gDao.find(bestGame.getGameID());
        assertNotNull(testGame1);
        assertEquals(bestGame, testGame1);
        Game testGame2 = gDao.find(game2.getGameID());
        assertNotNull(testGame2);
        assertEquals(game2, testGame2);
        //Then we tell the gameDAO to clear the table, and look for the same games, which should no longer be there
        gDao.clear();
        testGame1 = gDao.find(bestGame.getGameID());
        assertNull(testGame1);
        testGame2 = gDao.find(game2.getGameID());
        assertNull(testGame2);
    }

    @Test
    //Checks to see if the remove method works the way it's supposed to
    public void removeTestPass() throws DataAccessException {
        //Insert game into the database
        gDao.insert(bestGame);
        //Ensures that our game is in the database to begin with
        Game testGame = gDao.find(bestGame.getGameID());
        assertNotNull(testGame);
        assertEquals(bestGame, testGame);
        //Tells the gameDAO to delete this game
        gDao.remove(bestGame);
        //Checks that the game with the deleted associated username is gone
        testGame = gDao.find(bestGame.getGameID());
        assertNull(testGame);
    }

    @Test
    //Checks to see if the remove method fails the way it's supposed to
    public void removeTestFail() throws DataAccessException {
        //Insert game into the database
        gDao.insert(bestGame);
        //Ensures that our game is in the database to begin with
        Game testGame = gDao.find(bestGame.getGameID());
        assertNotNull(testGame);
        assertEquals(bestGame, testGame);
        //Create additional game to ensure test works properly
        Game game2 = new Game(200, "Gale", "TnT", "All4One", new game.Game());
        //Tells the gameDAO to remove a game that is not in the database
        gDao.remove(game2);
        //Checks that no games were removed
        Game[] games = gDao.findAll();
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(bestGame, games[0]);
    }

    @Test
    public void updateTestPass() throws DataAccessException, InvalidMoveException {
        gDao.insert(bestGame);
        ChessGame currGame = bestGame.getGame();
        currGame.makeMove(new Move(new Position(1, 7), new Position(3, 6)));
        gDao.update(bestGame.getGameID(), currGame);
        Game testGame = new Game(bestGame.getGameID(), bestGame.getWhiteUsername(), bestGame.getBlackUsername(),
                bestGame.getGameName(), currGame);
        Game compareGame = gDao.find(bestGame.getGameID());
        assertNotNull(compareGame);
        assertEquals(testGame, compareGame);
    }

    @Test
    public void updateTestFail() throws DataAccessException, InvalidMoveException {
        gDao.insert(bestGame);
        assertNotNull(gDao.find(bestGame.getGameID()));
        assertEquals(gDao.find(bestGame.getGameID()), bestGame);
        ChessGame nextMove = bestGame.getGame();
        nextMove.makeMove(new Move(new Position(1, 7), new Position(3, 6)));
        gDao.update(3, nextMove);
        Game testGame = gDao.find(bestGame.getGameID());
        assertNotNull(testGame);
        assertEquals(testGame.getGameID(), bestGame.getGameID());
        assertEquals(testGame.getWhiteUsername(), bestGame.getWhiteUsername());
        assertEquals(testGame.getBlackUsername(), bestGame.getBlackUsername());
        assertEquals(testGame.getGameName(), bestGame.getGameName());
        assertNotEquals(testGame.getGame(), nextMove);
    }

    @Test
    public void claimSpotTestPass() throws DataAccessException, AlreadyTakenException {
        Game testGame = new Game(1234, null, null, "MyGame", new game.Game());
        gDao.insert(testGame);
        Game compareGame = gDao.find(testGame.getGameID());
        assertNotNull(compareGame);
        assertEquals(compareGame, testGame);
        assertNull(compareGame.getWhiteUsername());
        gDao.claimSpot(testGame.getGameID(), ChessGame.TeamColor.WHITE, "ThiefBob");
        compareGame = gDao.find(testGame.getGameID());
        assertNotNull(compareGame);
        assertEquals(compareGame.getGameID(), testGame.getGameID());
        assertEquals(compareGame.getGameName(), testGame.getGameName());
        assertEquals(compareGame.getGame(), testGame.getGame());
        assertNotNull(compareGame.getWhiteUsername());
        assertEquals("ThiefBob", compareGame.getWhiteUsername());
    }

    @Test
    public void claimSpotTestFail() throws DataAccessException {
        gDao.insert(bestGame);
        assertNotNull(gDao.find(bestGame.getGameID()));
        assertEquals(gDao.find(bestGame.getGameID()), bestGame);
        assertNotNull(gDao.find(bestGame.getGameID()).getWhiteUsername());
        assertThrows(AlreadyTakenException.class,
                () -> gDao.claimSpot(bestGame.getGameID(), ChessGame.TeamColor.WHITE, "Mario"));
    }
}
