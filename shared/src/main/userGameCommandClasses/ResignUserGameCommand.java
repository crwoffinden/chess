package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class ResignUserGameCommand extends UserGameCommand {
    public ResignUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
    }

    protected CommandType commandType = CommandType.RESIGN;

    private String authToken;

    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
