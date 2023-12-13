package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class LeaveUserGameCommand extends UserGameCommand {
    public LeaveUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
    }

    protected CommandType commandType = CommandType.LEAVE;

    private String authToken;

    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
