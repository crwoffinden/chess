package serverMessagesClasses;

public class NotificationServerMessage extends webSocketMessages.ServerMessage {
    public NotificationServerMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    protected ServerMessageType type;

    private String message;

    public String getMessage() {
        return message;
    }
}
