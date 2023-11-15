package dataAccess.model;

import dataAccess.AlreadyTakenException;

import java.util.Objects;

/**Represents a game object*/
public class Game {
    /**The gameID*/
    private int gameID;
    /**The username of the white player*/
    private String whiteUsername;
    /**The username of the black player*/
    private String blackUsername;
    /**The game name*/
    private String gameName;
    /**The chess game*/
    private chess.ChessGame game;

    /**Constructor
     *
     * @param gameID
     * @param whiteUsername
     * @param blackUsername
     * @param gameName
     * @param game
     */
    public Game(int gameID, String whiteUsername, String blackUsername, String gameName, chess.ChessGame game) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

    /**Returns Game ID
     *
     * @return
     */
    public int getGameID() {
        return gameID;
    }

    /**Returns the white player's username
     *
     * @return
     */
    public String getWhiteUsername() {
        return whiteUsername;
    }

    /**Returns the black player's username
     *
     * @return
     */

    public void setWhiteUsername(String username) throws AlreadyTakenException {
        if (whiteUsername != null) throw new AlreadyTakenException("Error: already taken");
        whiteUsername = username;
    }
    public String getBlackUsername(){
        return blackUsername;
    }

    public void setBlackUsername(String username) throws AlreadyTakenException {
        if (blackUsername != null) throw new AlreadyTakenException("Error: already taken");
        blackUsername = username;
    }

    /**Returns the game name
     *
     * @return
     */


    public String getGameName() {
        return gameName;
    }

    /**Returns the chess game
     *
     * @return
     */
    public chess.ChessGame getGame() {
        return game;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        if (((Game)obj).whiteUsername == null) {
            if (this.whiteUsername != null) return false;
        }
        else {
            if (this.whiteUsername == null) return false;
            if (!(((Game)obj).whiteUsername.equals(this.whiteUsername))) return false;
        }
        if (((Game)obj).blackUsername == null) {
            if (this.blackUsername != null) return false;
        }
        else {
            if (this.blackUsername == null) return false;
            if (!(((Game)obj).blackUsername.equals(this.blackUsername))) return false;
        }
        return ((((Game)obj).gameID == this.gameID) && (((Game)obj).gameName.equals(this.gameName))
                && (((Game)obj).game.equals(this.game)));
    }
}
