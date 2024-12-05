package server;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String username;
    public int gameID;
    public Session session;

    public Connection(int gameID, String username, Session session) {
        this.gameID = gameID;
        this.username =username;
        this.session = session;
    }
    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
    public String getUsername() {
        return username;
    }
}
