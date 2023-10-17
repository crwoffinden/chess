package dataAccess.result;

import dataAccess.model.Game;

import java.util.Map;

public class ListGamesResult {
    /**The list of games*/
    private Map<Integer, Game> games;

    /**Error message if error occurs*/
    private String message;

    /**Whether the List Games was successful*/
    private boolean success;

    /**Constructor
     *
     * @param games
     * @param message
     * @param success
     */
    public ListGamesResult(Map<Integer, Game> games, String message, boolean success) {
        this.games = games;
        this.message = message;
        this.success = success;
    }

    /**Gets the games list
     *
     * @return
     */
    public Map<Integer, Game> getGames() {
        return games;
    }

    /**Gets the error message
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**Returns whether the list games action was successful or not
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }
}
