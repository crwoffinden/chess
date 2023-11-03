package dataAccess.result;

/**The result of a create game request*/
public class CreateGameResult {
    /**The game ID of the new game*/
    private Integer gameID;

    /**The error message if an error occurs*/
    private String message;

    /**Whether the create game request is successful or not*/
    private boolean success;

    /**Constructor
     *
     * @param gameID
     * @param message
     * @param success
     */
    public CreateGameResult(Integer gameID, String message, boolean success) {
        this.gameID = gameID;
        this.message = message;
        this.success = success;
    }

    /**Gets the game ID
     *
     * @return
     */
    public int getGameID() {
        return gameID;
    }

    /**Gets the error message*/
    public String getMessage() {
        return message;
    }

    /**Returns whether the create game request was successful or not.
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
