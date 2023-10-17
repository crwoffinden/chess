package dataAccess.result;

/**The result of a login request*/
public class LoginResult {
    /**Username of the user*/
    private String username;

    /**Generated authtoken*/
    private String authtoken;

    /**Error message if an error occurs*/
    private String message;

    /**Whether the login succeeded or failed*/
    private boolean success;

    /**Constructor
     *
     * @param username
     * @param authtoken
     * @param message
     * @param success
     */
    public LoginResult(String username, String authtoken, String message, boolean success) {
        this.username = username;
        this.authtoken = authtoken;
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
    public String getAuthtoken() {
        return authtoken;
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
}
