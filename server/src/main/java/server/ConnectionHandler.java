package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String username, Session session) {
        var connection = new Connection(gameID, username, session);
        connections.put(username, connection);
    }

    public void remove(String username) {
        connections.remove(username);
    }
    public void removeConnections(int gameID) {
        for(var c:connections.values()) {
            if(c.gameID == gameID) {
                connections.remove(c.getUsername());
            }
        }
    }
    public void broadcast(int gameID, String excludeUsername, ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.username.equals(excludeUsername) && c.gameID == gameID) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.username);
        }
    }
    public void sendBadAuthOrID(Session session,ServerMessage serverMessage) throws IOException {
        Connection.sendBySession(session, new Gson().toJson(serverMessage));
    }
    /*public void broadcast(int gameID, String excludeUsername, ServerMessage serverMessage) throws IOException {
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                if(c.gameID == gameID && !c.getUsername().equals(excludeUsername)) {
                    c.send(new Gson().toJson(serverMessage));
                }
            } else {
                connections.remove(c.getUsername());
            }
        }
    }*/
    public void sendToOne(String username, ServerMessage serverMessage) throws IOException {
        var c = connections.get(username);
        c.send(new Gson().toJson(serverMessage));
    }
}
