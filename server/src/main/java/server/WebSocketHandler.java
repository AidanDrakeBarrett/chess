package server;

import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;

import javax.websocket.OnMessage;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionHandler connections = new ConnectionHandler();
    SQLAuthDAO authDAO = new SQLAuthDAO();

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()) {
            case CONNECT -> connect(session, command.getAuthToken(), command.getGameID());
        }
    }
    private void connect(Session session, String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        connections.add(gameID, username, session);

    }
}
