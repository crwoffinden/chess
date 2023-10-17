package dataAccess.result;

/**The result of an attempt to log a user out*/
public class LogoutResult {
    /**Error message if an error occurs*/
    private String message;

    /**Whether the logout was successful or not*/
    private boolean success;

    /**Constructor
     *
     * @param message
     * @param success
     */
    public LogoutResult(String message, boolean success) {
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

    /**Returns whether the logout was successful or not
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }
}
