package dataAccess.request;

import chess.ChessGame;
import dataAccess.model.User;

/**Request to join a game*/
public class JoinGameRequest {
    /**The specified player color*/
    private ChessGame.TeamColor playerColor;

    /**The game ID of the game being joined*/
    private int gameID;

    //FIXME used in memory implementation, may need to remove when adding actual database
    private String username;

    /**Constructor
     *
     * @param playerColor
     * @param gameID
     */
    public JoinGameRequest(ChessGame.TeamColor playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    /**Returns the player color
     *
     * @return
     */
    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }

    /**Gets the game ID
     *
     * @return
     */
    public int getGameID() {
        return gameID;
    }
}
