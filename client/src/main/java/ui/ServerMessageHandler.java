package ui;

import websocket.messages.*;

public interface ServerMessageHandler {
    void notify(ServerMessage message);
}
