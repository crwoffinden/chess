package model;

/**Represents a user object*/
public class User {
    /**The user's username*/
    private String username;
    /**The user's password*/
    private String password;
    /**The user's email*/
    private String email;

    /**Constructor
     *
     * @param username
     * @param password
     * @param email
     */
    public User(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**Gets the user's username
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**Gets the user's password
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**Get's the user's email
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        return ((((User)obj).username.equals(this.username))
                && (((User)obj).password.equals(this.password)) && (((User)obj).email.equals(this.email)));
    }
}
