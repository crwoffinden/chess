package request;

/**Request to create a new game*/
public class CreateGameRequest {
    /**The name of the new game*/
    private String gameName;

    /**Constructor
     *
     * @param gameName
     */
    public CreateGameRequest(String gameName) {
        this.gameName = gameName;
    }

    /**Gets the game name
     *
     * @return
     */
    public String getGameName() {
        return gameName;
    }
}
