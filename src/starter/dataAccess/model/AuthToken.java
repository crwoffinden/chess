package dataAccess.model;

/**Represents an authtoken object*/
public class AuthToken {
    /**The actual authtoken*/
    private String authToken;
    /**The associated username*/
    private String username;

    /**Constructor
     *
     * @param authToken
     * @param username
     */
    public AuthToken(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }

    /**Gets the authtoken
     *
     * @return
     */
    public String getAuthToken() {
        return authToken;
    }

    /**Returns the username
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        return ((((AuthToken)obj).authToken == this.authToken) && (((AuthToken)obj).username == this.username));
    }
}
