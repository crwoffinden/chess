package webSocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        return ((((Connection)obj).gameID == this.gameID) && (((Connection)obj).username.equals(this.username))
                && (((Connection)obj).session.equals(session)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, username, session);
    }
}