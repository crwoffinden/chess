package webSocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public int gameID;

    public String username;
    public Session session;

    public Connection(int gameID, String username, Session session) {
        this.gameID = gameID;
        this.username = username;
        this.session = session;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}