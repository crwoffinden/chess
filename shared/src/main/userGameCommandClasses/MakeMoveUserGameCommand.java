package userGameCommandClasses;

import chess.ChessMove;
import webSocketMessages.UserGameCommand;

public class MakeMoveUserGameCommand extends UserGameCommand {
    public MakeMoveUserGameCommand(String authToken, Integer gameID, ChessMove move) {
        super(authToken);
        this.gameID = gameID;
        this.move = move;
        commandType = CommandType.MAKE_MOVE;
    }

    private Integer gameID;

    private ChessMove move;

    public Integer getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return move;
    }
}
