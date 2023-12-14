package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class ResignUserGameCommand extends UserGameCommand {
    public ResignUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
        commandType = CommandType.RESIGN;
    }
    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
