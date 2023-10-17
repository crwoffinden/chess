package dataAccess.result;

/**The result of a request to register a new user*/
public class RegisterResult {
    /**Username of the new user*/
    private String username;

    /**Generated authtoken*/
    private String authtoken;

    /**Error message if an error occurs*/
    private String message;

    /**Whether the register succeeded or failed*/
    private boolean success;

    /**Constructor
     *
     * @param username
     * @param authtoken
     * @param message
     * @param success
     */
    public RegisterResult(String username, String authtoken, String message, boolean success) {
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

    /**Returns whether the register request was successful or not
     *
     * @return
     */
    public boolean isSuccess() {
        return success;
    }
}
