package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import records.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.OnMessage;
import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionHandler connections = new ConnectionHandler();
    SQLAuthDAO authDAO = new SQLAuthDAO();
    SQLGameDAO gameDAO = new SQLGameDAO();

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()) {
            case CONNECT -> connect(session, command.getAuthToken(), command.getGameID());
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove());
            case RESIGN -> resign(command.getAuthToken(), command.getGameID());
        }
    }
    private void connect(Session session, String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        connections.add(gameID, username, session);
        String userJoined;
        if(gameDAO.getGame(gameID).blackUsername().equals(username)) {
            userJoined = username + " joined the game as black\n";
        } else if(gameDAO.getGame(gameID).whiteUsername().equals(username)) {
            userJoined = username + " joined the game as white\n";
        } else {
            userJoined = username + " joined the game as an observer\n";
        }
        ServerMessage joinNotice = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, userJoined);
        ServerMessage gameLoad = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        try {
            connections.broadcast(gameID, username, joinNotice);
            connections.sendToOne(username, gameLoad);
        } catch(IOException e) {}
    }
    private void makeMove(String authToken, int gameID, ChessMove move) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        ChessGame theGame = game.chessGame();
        try {
            theGame.makeMove(move);
            gameDAO.updateBoard(gameID, theGame);
        } catch(InvalidMoveException e) {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            try {
                connections.sendToOne(username, error);
            } catch(IOException ex) {}
        }
        GameData updatedGame = gameDAO.getGame(gameID);
        ServerMessage gameUpdate = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);
        try {
            connections.broadcast(gameID, null, gameUpdate);
        } catch(IOException e) {}
    }
    private void resign(String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        String letterOfRes = String.format("%s has resigned\n", username);
        String acceptRes = "You have resigned\n";
        ServerMessage resignation = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, letterOfRes);
        ServerMessage acceptance = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, acceptRes);
        try {
            connections.broadcast(gameID, username, resignation);
            connections.sendToOne(username, acceptance);
        } catch(IOException e) {}
        gameDAO.endGame(gameID);
        connections.remove(username);
    }
}
