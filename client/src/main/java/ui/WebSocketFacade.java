package ui;

import javax.websocket.*;

import records.ResponseException;
import websocket.*;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;

    public WebSocketFacade(String url) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    ServerMessageHandler.notify(notification);//FIXME: FATHER, HELP. i MUST BE IMPLEMENTED, EVEN THOUGH I AM ANNOYING AS SHIT.
                }
            });
        } catch(DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
