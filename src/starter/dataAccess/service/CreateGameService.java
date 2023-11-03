package dataAccess.service;

import dataAccess.DAO.AuthTokenDAO;
import dataAccess.DAO.Database;
import dataAccess.DAO.GameDAO;
import dataAccess.DataAccessException;
import dataAccess.model.Game;
import dataAccess.request.CreateGameRequest;
import dataAccess.result.CreateGameResult;

/**Creates a new game*/
public class CreateGameService {
    /**Creates a new game
     *
     * @param r
     * @return
     */
    //FIXME this is memory implementation fix when you add the actual database
    public CreateGameResult createGame(CreateGameRequest r, String authtoken, Database db) {
        GameDAO gDAO = db.getGameDAO();
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
        }
    }
}
