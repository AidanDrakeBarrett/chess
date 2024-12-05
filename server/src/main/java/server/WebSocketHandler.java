package server;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import javax.websocket.OnMessage;

@WebSocket
public class WebSocketHandler {
    private final ConnectionHandler connections = new ConnectionHandler();

    @OnMessage
    public void
}
