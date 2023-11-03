package passoffTests.webAPITests;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DAO.UserDAO;
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

import java.util.HashMap;
import java.util.Map;

public class serviceTests {
    //FIXME memory implementation adjust after adding the actual database
    private Database db = new Database(new UserDAO(), new GameDAO(), new AuthTokenDAO());

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
    public void setup() {
        clearService.clear(db);
    }

    @AfterEach
    public void takedown() {
        clearService.clear(db);
    }

    @Test
    public void clearTest() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        String username = r.getUsername();
        String authToken = r.getAuthToken();
        CreateGameResult g = createService.createGame(firstGame, authToken, db);
        int gameID = g.getGameID();
        assertNotNull(username);
        assertNotNull(db.getUserDAO().find(username));
        assertNotNull(gameID);
        assertNotNull(db.getGameDAO().find(gameID));
        assertNotNull(authToken);
        assertNotNull(db.getAuthTokenDAO().find(authToken));
        clearService.clear(db);
        assertThrows(DataAccessException.class, () -> db.getUserDAO().find(username),
                "No user with that username.");
        assertThrows(DataAccessException.class, () -> db.getGameDAO().find(gameID),
                "No games with that ID.");
        assertThrows(DataAccessException.class, () -> db.getAuthTokenDAO().find(authToken),
                "Authtoken not found.");
    }

    @Test
    public void registerTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        String username = r.getUsername();
        String authToken = r.getAuthToken();
        assertNotNull(username);
        assertNotNull(db.getUserDAO().find(username));
        assertNotNull(authToken);
        assertNotNull(db.getAuthTokenDAO().find(authToken));
        RegisterResult newRes = registerService.register(secondRegister, db);
        String otherUsername = newRes.getUsername();
        String otherAuthtoken = newRes.getAuthToken();
        assertNotNull(otherUsername);
        assertNotNull(otherAuthtoken);
        assertNotNull(db.getUserDAO().find(otherUsername));
        assertNotNull(db.getAuthTokenDAO().find(otherAuthtoken));
    }

    @Test
    public void registerTestFail() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        String username = r.getUsername();
        String authtoken = r.getAuthToken();
        assertNotNull(username);
        assertNotNull(db.getUserDAO().find(username));
        assertNotNull(authtoken);
        assertNotNull(db.getAuthTokenDAO().find(authtoken));
        RegisterRequest badRegister = new RegisterRequest("TheifBob", "abc123",
                "a.nother.email");
        RegisterResult badRes = registerService.register(badRegister, db);
        assertEquals("Error: already taken", badRes.getMessage());
    }

    @Test
    public void loginTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        String username = firstRegister.getUsername();
        String password = firstRegister.getPassword();
        assertNotNull(db.getUserDAO().find(username));
        LoginResult l = loginService.login(new LoginRequest(username, password), db);
        String authtoken = l.getAuthToken();
        assertNotNull(authtoken);
        assertNotNull(db.getAuthTokenDAO().find(authtoken));
    }

    @Test
    public void loginTestNoAccountFail() throws DataAccessException {
        registerService.register(firstRegister, db);
        assertNotNull(db.getUserDAO().find(firstRegister.getUsername()));
        LoginResult l = loginService.login(new LoginRequest("NotARobot", "password"), db);
        assertEquals("Error: unauthorized", l.getMessage());
    }

    @Test
    public void loginTestWrongPasswordFail() throws DataAccessException {
        registerService.register(firstRegister, db);
        assertNotNull(db.getUserDAO().find(firstRegister.getUsername()));
        LoginResult l = loginService.login(new LoginRequest(firstRegister.getUsername(), "notMyPassword"), db);
        assertEquals("Error: unauthorized", l.getMessage());
    }

    @Test
    public void logoutTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        String authtoken = r.getAuthToken();
        assertNotNull(db.getUserDAO().find(firstRegister.getUsername()));
        assertNotNull(authtoken);
        assertNotNull(db.getAuthTokenDAO().find(authtoken));
        logoutService.logout(authtoken, db);
        assertNotNull(db.getUserDAO().find(firstRegister.getUsername()));
        assertThrows(DataAccessException.class, () -> db.getAuthTokenDAO().find(authtoken),
                "Authtoken not found");
    }

    @Test
    public void logoutTestFail() throws DataAccessException {
        assertEquals("Error: bad request", logoutService.logout("12345", db).getMessage());
    }

    @Test
    public void listGamesTestPass() throws DataAccessException {
        RegisterResult res = registerService.register(firstRegister, db);
        CreateGameResult first = createService.createGame(firstGame, res.getAuthToken(), db);
        CreateGameResult second = createService.createGame(secondGame, res.getAuthToken(), db);
        assertNotNull(first);
        assertNotNull(second);
        Game gameOne = new Game(first.getGameID(), null, null, firstGame.getGameName(),
                new game.Game());
        Game gameTwo = new Game(second.getGameID(), null, null, secondGame.getGameName(),
                new game.Game());
        Game[] expectedGames = new Game[2];
        expectedGames[0] = gameOne;
        expectedGames[1] = gameTwo;
        Game[] actualGames = listService.listGames(res.getAuthToken(), db).getGames();
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
        RegisterResult res = registerService.register(firstRegister, db);
        CreateGameResult first = createService.createGame(firstGame, res.getAuthToken(), db);
        CreateGameResult second = createService.createGame(secondGame, res.getAuthToken(), db);
        assertNotNull(first);
        assertNotNull(second);
        logoutService.logout(res.getAuthToken(), db);
        ListGamesResult games = listService.listGames(res.getAuthToken(), db);
        assertFalse(games.isSuccess());
        assertEquals("Error: unauthorized", games.getMessage());
    }

    @Test
    public void createGameTestPass() throws DataAccessException {
        RegisterResult res = registerService.register(firstRegister, db);
        int gameID = createService.createGame(firstGame, res.getAuthToken(), db).getGameID();
        assertNotNull(gameID);
        assertNotNull(db.getGameDAO().find(gameID));
        assertEquals(db.getGameDAO().find(gameID).getGameName(), firstGame.getGameName());
    }

    @Test
    public void createGameTestFail() {
        CreateGameResult res = createService.createGame(firstGame, "badauthtoken", db);
        assertFalse(res.isSuccess());
        assertEquals("Error: unauthorized", res.getMessage());
    }

    @Test
    public void joinGameTestPass() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        int gameID = createService.createGame(firstGame, r.getAuthToken(), db).getGameID();
        joinService.joinGame(new JoinGameRequest(WHITE, gameID), r.getAuthToken(), db);
        Game thisGame = db.getGameDAO().find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
        RegisterResult newRes = registerService.register(secondRegister, db);
        joinService.joinGame(new JoinGameRequest(BLACK, gameID), newRes.getAuthToken(), db);
        thisGame = db.getGameDAO().find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertEquals(secondRegister.getUsername(), thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
    }

    @Test
    public void joinGameTestBadRequestFail() {
        RegisterResult r = registerService.register(firstRegister, db);
        JoinGameResult j = joinService.joinGame(new JoinGameRequest(WHITE, 12345), r.getAuthToken(), db);
        assertEquals("Error: bad request", j.getMessage());
    }

    @Test
    public void joinGameAlreadyTakenFail() throws DataAccessException {
        RegisterResult r = registerService.register(firstRegister, db);
        int gameID = createService.createGame(firstGame, r.getAuthToken(), db).getGameID();
        joinService.joinGame(new JoinGameRequest(WHITE, gameID), r.getAuthToken(), db);
        Game thisGame = db.getGameDAO().find(gameID);
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(firstRegister.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(firstGame.getGameName(), thisGame.getGameName());
        RegisterResult newRes = registerService.register(secondRegister, db);
        JoinGameResult jRes = joinService.joinGame(new JoinGameRequest(WHITE, gameID), newRes.getAuthToken(), db);
        assertEquals("Error: already taken", jRes.getMessage());
    }
}
