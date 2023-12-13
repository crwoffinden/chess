package userGameCommandClasses;

import webSocketMessages.UserGameCommand;

public class JoinObserverUserGameCommand extends UserGameCommand {
    public JoinObserverUserGameCommand(String authToken, Integer gameID) {
        super(authToken);
        this.gameID = gameID;
    }

    protected CommandType commandType = CommandType.JOIN_OBSERVER;

    private String authToken;

    private Integer gameID;

    public Integer getGameID() {
        return gameID;
    }
}
