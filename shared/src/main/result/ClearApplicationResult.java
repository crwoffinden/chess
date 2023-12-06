package result;

/**The result of an attempt to clear the application of all data*/
public class ClearApplicationResult {
    /**The message passed after a clear request*/
    private String message;

    /**Whether the clear was successful or not*/
    private boolean success;

    /**Constructor
     *
     * @param message
     * @param success
     */
    public ClearApplicationResult(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    /**Gets the error message
     *
     * @return
     */
    public String getMessage() {
        return  message;
    }

    /**Returns whether the clear was successful or not
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
