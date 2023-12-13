package serverMessagesClasses;

public class ErrorServerMessage extends webSocketMessages.ServerMessage {
    public ErrorServerMessage(ServerMessageType type, String errorMessage) {
        super(type);
        this.errorMessage = errorMessage;
    }

    protected ServerMessageType type;

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }
}
