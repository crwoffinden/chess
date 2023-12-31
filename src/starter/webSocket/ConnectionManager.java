package webSocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public enum Recipients {
        ONLY_USER,
        NOT_USER,
        EVERYONE
    }

    public void add(int gameID, String username, Session session) {
        var connection = new Connection(gameID, username, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }

    public void broadcast(int gameID, String root, Recipients type , String message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            //Ensures only people watching the same game get the messages
            if (c.gameID == gameID && c.session.isOpen()) {
                if (c.username.equals(root)) {
                    //Will send to root user unless it specifically says not to
                    if (type != Recipients.NOT_USER) c.send(message);
                }
                else {
                    //Will send to other users unless it specifically sends only to the root user
                    if (type != Recipients.ONLY_USER) c.send(message);
                }
            } else if (!c.session.isOpen()){
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
}
