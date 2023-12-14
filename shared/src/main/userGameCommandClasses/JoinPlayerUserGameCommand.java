package userGameCommandClasses;

import chess.ChessGame;
import webSocketMessages.UserGameCommand;

public class JoinPlayerUserGameCommand extends UserGameCommand {
    public JoinPlayerUserGameCommand(String authToken, Integer gameID, ChessGame.TeamColor playerColor) {
        super(authToken);
        this.gameID = gameID;
        this.playerColor = playerColor;
        commandType = CommandType.JOIN_PLAYER;
    }

    private Integer gameID;

    private ChessGame.TeamColor playerColor;

    public Integer getGameID() {
        return gameID;
    }

    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }
}
