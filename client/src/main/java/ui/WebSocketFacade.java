package ui;

import javax.websocket.*;

import chess.ChessGame;
import records.JoinRequests;
import records.ResponseException;
import com.google.gson.Gson;
import websocket.messages.*;
import websocket.commands.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private ServerMessageHandler serverMessageHandler;
    private int gameID;
    private String authToken;
    private ChessGame chessGame = null;

    public WebSocketFacade(String url, String authToken, int gameID) throws ResponseException {
        this.authToken = authToken;
        this.gameID = gameID;
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    serverMessageHandler.notify(serverMessage);
                }
            });
        } catch(DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
    public void joinGame(UserGameCommand.CommandType commtype) throws ResponseException {
        try {
            var command = new UserGameCommand(commtype, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch(IOException e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
    public void makeMove() {}
    public void resign() {}
    public void leave() {}
    public ChessGame getChessGame() {
        return chessGame;
    }
}
