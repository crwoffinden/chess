package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class LeaveUserGameCommand extends UserGameCommand {
    public LeaveUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
        commandType = CommandType.LEAVE;
    }

    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
