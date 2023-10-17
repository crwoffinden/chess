package dataAccess.result;

public class JoinGameResult {
    /**Error message if an error occurs*/
    private String message;

    /**Whether the join game request was successful or not*/
    private boolean success;

    /**Constructor
     *
     * @param message
     * @param success
     */
    public JoinGameResult(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    /**Returns the error message
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**Returns whether the join game request was successful or not
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }
}
