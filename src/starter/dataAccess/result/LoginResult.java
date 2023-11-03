package dataAccess.result;

/**The result of a login request*/
public class LoginResult {
    /**Username of the user*/
    private String username;

    /**Generated authtoken*/
    private String authToken;

    /**Error message if an error occurs*/
    private String message;

    /**Whether the login succeeded or failed*/
    private boolean success;

    /**Constructor
     *
     * @param username
     * @param authToken
     * @param message
     * @param success
     */
    public LoginResult(String username, String authToken, String message, boolean success) {
        this.username = username;
        this.authToken = authToken;
        this.message = message;
        this.success = success;
    }

    /**Gets the username
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**Gets the authtoken
     *
     * @return
     */
    public String getAuthToken() {
        return authToken;
    }

    /**Gets the error message
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**Returns whether the login request was successful or not
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
