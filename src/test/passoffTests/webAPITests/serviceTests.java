package passoffTests.webAPITests;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import dataAccess.model.Game;
import dataAccess.request.CreateGameRequest;
import dataAccess.request.JoinGameRequest;
import dataAccess.request.LoginRequest;
import dataAccess.request.RegisterRequest;
import dataAccess.result.*;
import dataAccess.service.*;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class serviceTests {
    //FIXME memory implementation adjust after adding the actual database
    private Database db = new Database();

    private Connection conn;

    private UserDAO uDAO;

    private GameDAO gDAO;

    private AuthTokenDAO aDAO;

    private ClearApplicationService clearService = new ClearApplicationService();

    private RegisterService registerService = new RegisterService();

    private LoginService loginService = new LoginService();

    private LogoutService logoutService = new LogoutService();

    private ListGamesService listService = new ListGamesService();

    private CreateGameService createService = new CreateGameService();

    private JoinGameService joinService = new JoinGameService();

    private RegisterRequest firstRegister = new RegisterRequest("TheifBob", "abc123",
            "TheifBob@mail.com");

    private RegisterRequest secondRegister = new RegisterRequest("NotARobot", "password",
            "email");

    private CreateGameRequest firstGame = new CreateGameRequest("myGame");

    private CreateGameRequest secondGame = new CreateGameRequest("anotherGame");

    //Add game data

    @BeforeEach
    public void setup() throws DataAccessException, SQLException {
        conn = db.getConnection();
        db.configureDatabase();
        uDAO = new UserDAO(conn);
        gDAO = new GameDAO(conn);
        aDAO = new AuthTokenDAO(conn);
        //Clears the database
        clearService.clear();
    }

    @AfterEach
    public void takedown() throws DataAccessException {
        db.closeConnection(db.getConnection());
    }

    @Test
    public void clearTest() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        String username = r.getUsername();
        String authToken = r.getAuthToken();
        CreateGameResult g = createService.createGame(firstGame, authToken);
        int gameID = g.getGameID();
        assertNotNull(username);
        assertNotNull(uDAO.find(username));
        assertNotNull(gameID);
        assertNotNull(gDAO.find(gameID));
        assertNotNull(authToken);
        assertNotNull(aDAO.find(authToken));
        clearService.clear();
        assertThrows(DataAccessException.class, () -> uDAO.find(username),
                "No user with that username.");
        assertThrows(DataAccessException.class, () -> gDAO.find(gameID),
                "No games with that ID.");
        assertThrows(DataAccessException.class, () -> aDAO.find(authToken),
                "Authtoken not found.");
    }

    @Test
    public void registerTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        String username = r.getUsername();
        String authToken = r.getAuthToken();
        assertNotNull(username);
        assertNotNull(uDAO.find(username));
        assertNotNull(authToken);
        assertNotNull(aDAO.find(authToken));
        RegisterResult newRes = registerService.register(secondRegister);
        String otherUsername = newRes.getUsername();
        String otherAuthtoken = newRes.getAuthToken();
        assertNotNull(otherUsername);
        assertNotNull(otherAuthtoken);
        assertNotNull(uDAO.find(otherUsername));
        assertNotNull(aDAO.find(otherAuthtoken));
    }

    @Test
    public void registerTestFail() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        String username = r.getUsername();
        String authtoken = r.getAuthToken();
        assertNotNull(username);
        assertNotNull(uDAO.find(username));
        assertNotNull(authtoken);
        assertNotNull(aDAO.find(authtoken));
        RegisterRequest badRegister = new RegisterRequest("TheifBob", "abc123",
                "a.nother.email");
        RegisterResult badRes = registerService.register(badRegister);
        assertEquals("Error: already taken", badRes.getMessage());
    }

    @Test
    public void loginTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        String username = firstRegister.getUsername();
        String password = firstRegister.getPassword();
        assertNotNull(uDAO.find(username));
        LoginResult l = loginService.login(new LoginRequest(username, password));
        String authtoken = l.getAuthToken();
        assertNotNull(authtoken);
        assertNotNull(aDAO.find(authtoken));
    }

    @Test
    public void loginTestNoAccountFail() throws DataAccessException {
        registerService.register(firstRegister);
        assertNotNull(uDAO.find(firstRegister.getUsername()));
        LoginResult l = loginService.login(new LoginRequest("NotARobot", "password"));
        assertEquals("Error: unauthorized", l.getMessage());
    }

    @Test
    public void loginTestWrongPasswordFail() throws DataAccessException {
        registerService.register(firstRegister);
        assertNotNull(uDAO.find(firstRegister.getUsername()));
        LoginResult l = loginService.login(new LoginRequest(firstRegister.getUsername(), "notMyPassword"));
        assertEquals("Error: unauthorized", l.getMessage());
    }

    @Test
    public void logoutTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        String authtoken = r.getAuthToken();
        assertNotNull(uDAO.find(firstRegister.getUsername()));
        assertNotNull(authtoken);
        assertNotNull(aDAO.find(authtoken));
        logoutService.logout(authtoken);
        assertNotNull(uDAO.find(firstRegister.getUsername()));
        assertThrows(DataAccessException.class, () -> aDAO.find(authtoken),
                "Authtoken not found");
    }

    @Test
    public void logoutTestFail() throws DataAccessException {
        assertEquals("Error: bad request", logoutService.logout("12345").getMessage());
    }

    @Test
    public void listGamesTestPass() throws DataAccessException {
        RegisterResult res = registerService.register(firstRegister);
        CreateGameResult first = createService.createGame(firstGame, res.getAuthToken());
        CreateGameResult second = createService.createGame(secondGame, res.getAuthToken());
        assertNotNull(first);
        assertNotNull(second);
        Game gameOne = new Game(first.getGameID(), null, null, firstGame.getGameName(),
                new game.Game());
        Game gameTwo = new Game(second.getGameID(), null, null, secondGame.getGameName(),
                new game.Game());
        Game[] expectedGames = new Game[2];
        expectedGames[0] = gameOne;
        expectedGames[1] = gameTwo;
        Game[] actualGames = listService.listGames(res.getAuthToken()).getGames();
        assertEquals(expectedGames.length, actualGames.length);
        for (int i = 0; i < actualGames.length; ++i) {
            boolean containsGame = false;
            for (int j = 0; j < expectedGames.length; ++j) {
                if (expectedGames[j].equals(actualGames[i])) containsGame = true;
            }
            assertTrue(containsGame);
        }
    }

    @Test
    public void listGamesTestFail() {
        RegisterResult res = registerService.register(firstRegister);
        CreateGameResult first = createService.createGame(firstGame, res.getAuthToken());
        CreateGameResult second = createService.createGame(secondGame, res.getAuthToken());
        assertNotNull(first);
        assertNotNull(second);
        logoutService.logout(res.getAuthToken());
        ListGamesResult games = listService.listGames(res.getAuthToken());
        assertFalse(games.isSuccess());
        assertEquals("Error: unauthorized", games.getMessage());
    }

    @Test
    public void createGameTestPass() throws DataAccessException {
        RegisterResult res = registerService.register(firstRegister);
        int gameID = createService.createGame(firstGame, res.getAuthToken()).getGameID();
        assertNotNull(gameID);
        assertNotNull(gDAO.find(gameID));
        assertEquals(gDAO.find(gameID).getGameName(), firstGame.getGameName());
    }

    @Test
    public void createGameTestFail() {
        CreateGameResult res = createService.createGame(firstGame, "badauthtoken");
        assertFalse(res.isSuccess());
        assertEquals("Error: unauthorized", res.getMessage());
    }

    @Test
    public void joinGameTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        int gameID = createService.createGame(firstGame, r.getAuthToken()).getGameID();
        joinService.joinGame(new JoinGameRequest(WHITE, gameID), r.getAuthToken());
        Game thisGame = gDAO.find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
        RegisterResult newRes = registerService.register(secondRegister);
        joinService.joinGame(new JoinGameRequest(BLACK, gameID), newRes.getAuthToken());
        thisGame = gDAO.find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertEquals(secondRegister.getUsername(), thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
    }

    @Test
    public void joinGameTestBadRequestFail() {
        RegisterResult r = registerService.register(firstRegister);
        JoinGameResult j = joinService.joinGame(new JoinGameRequest(WHITE, 12345), r.getAuthToken());
        assertEquals("Error: bad request", j.getMessage());
    }

    @Test
    public void joinGameAlreadyTakenFail() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister);
        int gameID = createService.createGame(firstGame, r.getAuthToken()).getGameID();
        joinService.joinGame(new JoinGameRequest(WHITE, gameID), r.getAuthToken());
        Game thisGame = gDAO.find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
        RegisterResult newRes = registerService.register(secondRegister);
        JoinGameResult jRes = joinService.joinGame(new JoinGameRequest(WHITE, gameID), newRes.getAuthToken());
        assertEquals("Error: already taken", jRes.getMessage());
    }
}
