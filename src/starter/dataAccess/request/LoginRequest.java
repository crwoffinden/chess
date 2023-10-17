package dataAccess.request;

/**Request to log a user in*/
public class LoginRequest {
    /**The given username*/
    private String username;

    /**The given password*/
    private String password;

    /**Constructor
     *
     * @param username
     * @param password
     */
    public LoginRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    /**Returns the username
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**Returns the password
     *
     * @return
     */
    public String getPassword() {
        return password;
    }
}
