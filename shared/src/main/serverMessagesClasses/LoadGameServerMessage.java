package serverMessagesClasses;

import chess.ChessGame;

public class LoadGameServerMessage extends webSocketMessages.ServerMessage {
    public LoadGameServerMessage(ServerMessageType type, ChessGame game) {
        super(type);
        this.game = game;
    }

    protected ServerMessageType type;

    private ChessGame game;

    public ChessGame getGame() {
        return game;
    }
}
