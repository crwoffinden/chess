package result;

import model.Game;

/**The result of an attempt to list all of the games*/
public class ListGamesResult {
    /**The list of games*/
    private Game[] games;

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
    public ListGamesResult(Game[] games, String message, boolean success) {
        this.games = games;
        this.message = message;
        this.success = success;
    }

    /**Gets the games list
     *
     * @return
     */
    public Game[] getGames() {
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

    //Determines if an error was the client's fault or the server's
    public boolean serverError() {
        return (message.equals("Error: Internal Server Error"));
    }
}
