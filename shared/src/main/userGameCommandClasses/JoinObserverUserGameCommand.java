package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class JoinObserverUserGameCommand extends UserGameCommand {
    public JoinObserverUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        commandType = CommandType.JOIN_OBSERVER;
        this.gameID = gameID;
    }
    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
