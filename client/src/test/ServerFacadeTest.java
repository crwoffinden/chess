import chess.*;
import com.google.gson.*;
import model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;
import request.JoinGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.*;

import java.io.IOException;
import java.lang.reflect.Type;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerFacadeTest {
    //Predesigned register and create game requests
    private final RegisterRequest FIRST_REGISTER = new RegisterRequest("TheifBob", "abc123",
            "TheifBob@mail.com");

    private final RegisterRequest SECOND_REGISTER = new RegisterRequest("NotARobot", "password",
            "email");

    private final CreateGameRequest FIRST_GAME = new CreateGameRequest("myGame");

    private final CreateGameRequest SECOND_GAME = new CreateGameRequest("anotherGame");

    //Url string
    private String url = "http://localhost:8080";

    //The actual ServerFacade
    private final ServerFacade SERVER = new ServerFacade();

    //Adapter classes
    class PositionAdapter implements JsonDeserializer<ChessPosition> {

        @Override
        public ChessPosition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Position.class);
        }
    }

    class MoveAdapter implements JsonDeserializer<ChessMove> {

        @Override
        public ChessMove deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Move.class);
        }
    }

    class PieceAdapter implements JsonDeserializer<ChessPiece> {

        @Override
        public ChessPiece deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Piece.class);
        }
    }

    class BoardAdapter implements JsonDeserializer<ChessBoard> {

        @Override
        public ChessBoard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Board.class);
        }
    }

    class GameAdapter implements JsonDeserializer<ChessGame> {

        @Override
        public ChessGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return jsonDeserializationContext.deserialize(jsonElement, game.Game.class);
        }
    }

    //Gson
    private final GsonBuilder BUILDER = new GsonBuilder();

    private Gson gson;

    @BeforeEach
    public void setup() throws IOException{
        //register adapter classes and create gson
        BUILDER.registerTypeAdapter(ChessPosition.class, new ClientMain.PositionAdapter());
        BUILDER.registerTypeAdapter(ChessMove.class, new ClientMain.MoveAdapter());
        BUILDER.registerTypeAdapter(ChessPiece.class, new ClientMain.PieceAdapter());
        BUILDER.registerTypeAdapter(ChessBoard.class, new ClientMain.BoardAdapter());
        BUILDER.registerTypeAdapter(ChessGame.class, new ClientMain.GameAdapter());
        gson = BUILDER.create();
        //Clears the database
        SERVER.deleteRequest(url + "/db", null);
    }

    @Test
    public void clearTest() throws IOException {
        //registers a new user and ensures that the register was successful
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //creates a game and ensures the game exists
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult l = gson.fromJson(responseJSON, ListGamesResult.class);
        Game[] games = l.getGames();
        int gameID = g.getGameID();
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(gameID, games[0].getGameID());
        //Clears the database
        responseJSON = SERVER.deleteRequest(url + "/db", null);
        ClearApplicationResult c = gson.fromJson(responseJSON, ClearApplicationResult.class);
        assertTrue(c.isSuccess());
        //Ensures what was in the database before has been removed
        LoginRequest loginRequest = new LoginRequest(FIRST_REGISTER.getUsername(), FIRST_REGISTER.getPassword());
        json = gson.toJson(loginRequest);
        responseJSON = SERVER.postRequest(url + "/session", json, null);
        LoginResult loginRes = gson.fromJson(responseJSON, LoginResult.class);
        assertFalse(loginRes.isSuccess());
    }

    @Test
    public void registerTestPass() throws IOException {
        //registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        //ensures the register was successful, the same username is registered, and the authtoken exists
        assertTrue(r.isSuccess());
        assertEquals(FIRST_REGISTER.getUsername(), r.getUsername());
        assertNotNull(r.getAuthToken());
    }

    @Test
    public void registerTestFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        assertTrue(r.isSuccess());
        //Attempts to register a new user with an identical username
        RegisterRequest badRegister = new RegisterRequest("TheifBob", "abc123",
                "a.nother.email");
        json = gson.toJson(badRegister);
        responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult badRes = gson.fromJson(responseJson, RegisterResult.class);
        //Ensures the register failed
        assertFalse(badRes.isSuccess());
        assertEquals("Error: already taken", badRes.getMessage());
    }

    @Test
    public void loginTestPass() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        assertTrue(r.isSuccess());
        //Logs the user in
        LoginRequest login = new LoginRequest(FIRST_REGISTER.getUsername(), FIRST_REGISTER.getPassword());
        json = gson.toJson(login);
        responseJson = SERVER.postRequest(url + "/session", json, null);
        assertNotNull(responseJson);
        //Ensures the login was successful and an authtoken was created
        LoginResult loginRes = gson.fromJson(responseJson, LoginResult.class);
        assertTrue(loginRes.isSuccess());
        String authtoken = loginRes.getAuthToken();
        assertNotNull(authtoken);
    }

    @Test
    public void loginTestNoAccountFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        assertTrue(r.isSuccess());
        //Attempts to log in an unregistered user
        LoginRequest badLogin = new LoginRequest("NotARobot", "password");
        json = gson.toJson(badLogin);
        responseJson = SERVER.postRequest(url + "/session", json, null);
        assertNotNull(responseJson);
        LoginResult lRes = gson.fromJson(responseJson, LoginResult.class);
        //Ensures the login failed
        assertFalse(lRes.isSuccess());
        assertEquals("Error: unauthorized", lRes.getMessage());
    }

    @Test
    public void loginTestWrongPasswordFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        assertTrue(r.isSuccess());
        //Attempts to log the user in with the wrong password
        LoginRequest badLogin = new LoginRequest(FIRST_REGISTER.getUsername(), "notMyPassword");
        json = gson.toJson(badLogin);
        responseJson = SERVER.postRequest(url + "/session", json, null);
        assertNotNull(responseJson);
        LoginResult lRes = gson.fromJson(responseJson, LoginResult.class);
        //Ensures the login failed
        assertFalse(lRes.isSuccess());
        assertEquals("Error: unauthorized", lRes.getMessage());
    }

    @Test
    public void logoutTestPass() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJson = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJson);
        RegisterResult r = gson.fromJson(responseJson, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authtoken = r.getAuthToken();
        assertNotNull(authtoken);
        //Logs the user out
        responseJson = SERVER.deleteRequest(url + "/session", authtoken);
        LogoutResult l = gson.fromJson(responseJson, LogoutResult.class);
        assertTrue(l.isSuccess());
        //Ensures the old authtoken is no longer vaild
        responseJson = SERVER.getRequest(url + "/game", authtoken);
        assertNotNull(responseJson);
        ListGamesResult listRes = gson.fromJson(responseJson, ListGamesResult.class);
        assertFalse(listRes.isSuccess());
        assertEquals("Error: unauthorized", listRes.getMessage());
    }

    @Test
    public void logoutTestFail() throws IOException {
        //Attempts a logout with a bad authtoken
        String responseJson = SERVER.deleteRequest(url + "/session", "12345");
        assertNotNull(responseJson);
        LogoutResult l = gson.fromJson(responseJson, LogoutResult.class);
        //Ensures the logout failed
        assertFalse(l.isSuccess());
        assertEquals("Error: bad request", l.getMessage());
    }

    @Test
    public void listGamesTestPass() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Creates a new game
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g1 = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g1.isSuccess());
        //Gets the game list
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult l = gson.fromJson(responseJSON, ListGamesResult.class);
        Game[] games = l.getGames();
        int gameID = g1.getGameID();
        //Ensures our list has the game we created
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(gameID, games[0].getGameID());
        //Creates a new game
        json = gson.toJson(SECOND_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g2 = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g2.isSuccess());
        //Lists all games
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        assertNotNull(responseJSON);
        ListGamesResult lUpdate = gson.fromJson(responseJSON, ListGamesResult.class);
        games = lUpdate.getGames();
        //Ensures both created games are in the list
        assertNotNull(games);
        assertEquals(2, games.length);
        assertTrue((g1.getGameID() == games[0].getGameID()) || (g1.getGameID() == games[1].getGameID()));
        assertTrue((g1.getGameID() == games[0].getGameID() && g2.getGameID() == games[1].getGameID()) ||
                (g1.getGameID() == games[1].getGameID() && g2.getGameID() == games[0].getGameID()));
    }

    @Test
    public void listGamesTestFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Creates new games
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g1 = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g1.isSuccess());
        json = gson.toJson(SECOND_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g2 = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g2.isSuccess());
        //Logs the user out
        responseJSON = SERVER.deleteRequest(url + "/session", authToken);
        assertNotNull(responseJSON);
        LogoutResult logoutRes = gson.fromJson(responseJSON, LogoutResult.class);
        assertTrue(logoutRes.isSuccess());
        //Attempts to list games while logged out
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        assertNotNull(responseJSON);
        ListGamesResult listRes = gson.fromJson(responseJSON, ListGamesResult.class);
        //Ensures the List Games request failed
        assertFalse(listRes.isSuccess());
        assertEquals("Error: unauthorized", listRes.getMessage());
    }

    @Test
    public void createGameTestPass() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Creates a new game
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g1 = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g1.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        //Ensures our game is in the database
        ListGamesResult l = gson.fromJson(responseJSON, ListGamesResult.class);
        Game[] games = l.getGames();
        int gameID = g1.getGameID();
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(gameID, games[0].getGameID());
    }

    @Test
    public void createGameTestFail() throws IOException {
        //Attempts to create a new game with a bad authtoken
        String json = gson.toJson(FIRST_GAME);
        String responseJSON = SERVER.postRequest(url + "/game", json, "badauthtoken");
        CreateGameResult g = gson.fromJson(responseJSON, CreateGameResult.class);
        //Ensures the game was not created
        assertFalse(g.isSuccess());
        assertEquals("Error: unauthorized", g.getMessage());
    }

    @Test
    public void joinGameTestPass() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Creates a new game
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult l = gson.fromJson(responseJSON, ListGamesResult.class);
        Game[] games = l.getGames();
        int gameID = g.getGameID();
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(gameID, games[0].getGameID());
        //Joins the newly created game as white
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, gameID);
        json = gson.toJson(joinRequest);
        responseJSON = SERVER.putRequest(url + "/game", json, authToken);
        assertNotNull(responseJSON);
        //Ensures the join request worked, and that our user is now the white player in the game
        JoinGameResult joinRes = gson.fromJson(responseJSON, JoinGameResult.class);
        assertTrue(joinRes.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult lUpdate = gson.fromJson(responseJSON, ListGamesResult.class);
        games = lUpdate.getGames();
        Game thisGame = games[0];
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(FIRST_REGISTER.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(FIRST_GAME.getGameName(), thisGame.getGameName());
        //Registers a second user
        json = gson.toJson(SECOND_REGISTER);
        responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult newRegister = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(newRegister.isSuccess());
        String newAuthToken = newRegister.getAuthToken();
        //Has the second user join the game as black
        JoinGameRequest secondJoin = new JoinGameRequest(BLACK, gameID);
        json = gson.toJson(secondJoin);
        responseJSON = SERVER.putRequest(url + "/game", json, newAuthToken);
        //Ensures the join request worked and that both users are listed as playing the game
        assertNotNull(responseJSON);
        JoinGameResult newJoinRes = gson.fromJson(responseJSON, JoinGameResult.class);
        assertTrue(newJoinRes.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult newListUpdate = gson.fromJson(responseJSON, ListGamesResult.class);
        games = newListUpdate.getGames();
        thisGame = games[0];
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(FIRST_REGISTER.getUsername(), thisGame.getWhiteUsername());
        assertEquals(SECOND_REGISTER.getUsername(), thisGame.getBlackUsername());
        assertEquals(FIRST_GAME.getGameName(), thisGame.getGameName());
    }

    @Test
    public void joinGameTestBadRequestFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Attempts to join a game that doesn't exist
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, 12345);
        json = gson.toJson(joinRequest);
        responseJSON = SERVER.putRequest(url + "/game", json, authToken);
        assertNotNull(responseJSON);
        JoinGameResult j = gson.fromJson(responseJSON, JoinGameResult.class);
        //Ensures the join request failed
        assertFalse(j.isSuccess());
        assertEquals("Error: bad request", j.getMessage());
    }

    @Test
    public void joinGameAlreadyTakenFail() throws IOException {
        //Registers a new user
        String json = gson.toJson(FIRST_REGISTER);
        String responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult r = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(r.isSuccess());
        String authToken = r.getAuthToken();
        //Creates a new game
        json = gson.toJson(FIRST_GAME);
        responseJSON = SERVER.postRequest(url + "/game", json, authToken);
        CreateGameResult g = gson.fromJson(responseJSON, CreateGameResult.class);
        assertTrue(g.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult l = gson.fromJson(responseJSON, ListGamesResult.class);
        Game[] games = l.getGames();
        int gameID = g.getGameID();
        assertNotNull(games);
        assertEquals(1, games.length);
        assertEquals(gameID, games[0].getGameID());
        //Has the user join the game as white
        JoinGameRequest joinRequest = new JoinGameRequest(WHITE, gameID);
        json = gson.toJson(joinRequest);
        responseJSON = SERVER.putRequest(url + "/game", json, authToken);
        assertNotNull(responseJSON);
        JoinGameResult joinRes = gson.fromJson(responseJSON, JoinGameResult.class);
        assertTrue(joinRes.isSuccess());
        responseJSON = SERVER.getRequest(url + "/game", authToken);
        ListGamesResult lUpdate = gson.fromJson(responseJSON, ListGamesResult.class);
        games = lUpdate.getGames();
        Game thisGame = games[0];
        assertEquals(gameID, thisGame.getGameID());
        assertEquals(FIRST_REGISTER.getUsername(), thisGame.getWhiteUsername());
        assertNull(thisGame.getBlackUsername());
        assertEquals(FIRST_GAME.getGameName(), thisGame.getGameName());
        //Registers a second user
        json = gson.toJson(SECOND_REGISTER);
        responseJSON = SERVER.postRequest(url + "/user", json, null);
        assertNotNull(responseJSON);
        RegisterResult newRegister = gson.fromJson(responseJSON, RegisterResult.class);
        assertTrue(newRegister.isSuccess());
        String newAuthToken = newRegister.getAuthToken();
        //Has the new user attempt to join the same game as white
        JoinGameRequest newJoinRequest = new JoinGameRequest(WHITE, gameID);
        json = gson.toJson(newJoinRequest);
        responseJSON = SERVER.putRequest(url + "/game", json, newAuthToken);
        assertNotNull(responseJSON);
        JoinGameResult jRes = gson.fromJson(responseJSON, JoinGameResult.class);
        //Ensures the join request failed
        assertFalse(jRes.isSuccess());
        assertEquals("Error: already taken", jRes.getMessage());
    }
}
