package dataAccess.request;

public class RegisterRequest {
    /**The username of the new user*/
    private String username;

    /**The new user's password*/
    private String password;

    /**The new user's email*/
    private String email;

    /**Constructor
     *
     * @param username
     * @param password
     * @param email
     */
    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
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

    /**Returns the email
     *
     * @return
     */
    public String getEmail() {
        return email;
    }
}
