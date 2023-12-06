package dataAccess.service;

import dataAccess.DAO.*;
import dataAccess.DataAccessException;
import model.Game;
import request.CreateGameRequest;
import result.CreateGameResult;

import java.sql.Connection;

/**Creates a new game*/
public class CreateGameService {
    /**Creates a new game
     *
     * @param r
     * @return
     */
    public CreateGameResult createGame(CreateGameRequest r, String authtoken) {
        Database db = new Database();
        try { //Creates a new game and sends a response
            Connection conn = db.getConnection();
            GameDAO gDAO = new GameDAO(conn);
            AuthTokenDAO aDAO = new AuthTokenDAO(conn);
            aDAO.find(authtoken);
            int gameID = gDAO.getNewID();
            Game newGame = new Game(gameID, null, null, r.getGameName(), new game.Game());
            newGame.getGame().getBoard().resetBoard();
            gDAO.insert(newGame);
            db.closeConnection(conn);
            CreateGameResult res = new CreateGameResult(gameID, null, true);
            return res;
        } catch (DataAccessException e) {
            return new CreateGameResult(null, "Error: unauthorized", false);
        }
        /*GameDAO gDAO = db.getGameDAO();
        AuthTokenDAO aDAO = db.getAuthTokenDAO();
        try {
            aDAO.find(authtoken);
            int gameID = gDAO.getNewID();
            Game newGame = new Game(gameID, null, null, r.getGameName(), new game.Game());
            gDAO.insert(newGame);
            CreateGameResult res = new CreateGameResult(gameID, null, true);
            return res;
        } catch (DataAccessException e) {
            return new CreateGameResult(null, "Error: unauthorized", false);
        }*/
    }
}
