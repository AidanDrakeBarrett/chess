package server;

import chess.*;
import com.google.gson.Gson;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import records.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.OnMessage;
import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
    private final ConnectionHandler connections = new ConnectionHandler();
    SQLAuthDAO authDAO = new SQLAuthDAO();
    SQLGameDAO gameDAO = new SQLGameDAO();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()) {
            case CONNECT -> connect(session, command.getAuthToken(), command.getGameID());
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove());
            case RESIGN -> resign(command.getAuthToken(), command.getGameID());
            case LEAVE -> leave(command.getAuthToken(), command.getGameID());
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
        ChessPiece piece = theGame.getBoard().getPiece(move.getStartPosition());
        String movedPiece = pieceName(piece.getPieceType());
        String startingString = positionString(move.getStartPosition());
        String endingString = positionString(move.getEndPosition());
        boolean wCheck = false;
        boolean wCheckmate = false;
        boolean wStalemate = false;
        boolean bCheck = false;
        boolean bCheckmate = false;
        boolean bStalemate = false;

        try {
            theGame.makeMove(move);
            wCheck = theGame.isInCheck(ChessGame.TeamColor.WHITE);
            wCheckmate = theGame.isInCheckmate(ChessGame.TeamColor.WHITE);
            wStalemate = theGame.isInStalemate(ChessGame.TeamColor.WHITE);
            bCheck = theGame.isInCheck(ChessGame.TeamColor.BLACK);
            bCheckmate = theGame.isInCheckmate(ChessGame.TeamColor.BLACK);
            bStalemate = theGame.isInStalemate(ChessGame.TeamColor.BLACK);
            gameDAO.updateBoard(gameID, theGame);
        } catch(InvalidMoveException e) {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
            try {
                connections.sendToOne(username, error);
            } catch(IOException ex) {}
        }

        GameData updatedGame = gameDAO.getGame(gameID);
        ServerMessage gameUpdate = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, updatedGame);

        StringBuilder aftermath = new StringBuilder(String.format("%s moved %s from %s to %s", username, movedPiece,
                startingString, endingString));
        if(move.getPromotionPiece() != null) {
            aftermath.append(String.format(" and promoted it to %s", pieceName(move.getPromotionPiece())));
        }
        aftermath.append(".\n");
        if(wCheckmate) {
            aftermath.append("White is in checkmate.\n");
            gameDAO.endGame(gameID);
        }
        if(bCheckmate) {
            aftermath.append("Black is in checkmate.\n");
            gameDAO.endGame(gameID);
        }
        if(!wCheckmate && !bCheckmate) {
            if(wCheck) {
                aftermath.append("White is in check.\n");
            }
            if(bCheck) {
                aftermath.append("Black is in check.\n");
            }
            if(wStalemate && bStalemate) {
                aftermath.append("The game is in stalemate.\n");
                gameDAO.endGame(gameID);
            }
        }
        ServerMessage aftermathMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                aftermath.toString());

        try {
            connections.broadcast(gameID, null, gameUpdate);
            connections.broadcast(gameID, null, aftermathMessage);
        } catch(IOException e) {}
    }
    private String pieceName(ChessPiece.PieceType piece) {
        switch (piece) {
            case PAWN -> {
                return "pawn";
            }
            case ROOK -> {
                return "rook";
            }
            case KNIGHT -> {
                return "knight";
            }
            case BISHOP -> {
                return "bishop";
            }
            case QUEEN -> {
                return "queen";
            }
            case KING -> {
                return "king";
            }
        }
        return null;
    }
    private String positionString(ChessPosition position) {
        char file = (char) (position.getColumn() + 64);
        return String.format("%s%d", file, position.getRow());
    }
    private void resign(String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        String letterOfRes = String.format("%s has resigned\n", username);
        String acceptRes = "You have resigned\n";
        ServerMessage resignation = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, letterOfRes);
        ServerMessage acceptance = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, acceptRes);
        gameDAO.endGame(gameID);
        try {
            connections.broadcast(gameID, username, resignation);
            connections.sendToOne(username, acceptance);
        } catch(IOException e) {}
        connections.remove(username);
    }
    private void leave(String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        String colorColumn = null;
        if(Objects.equals(game.whiteUsername(), username)) {
            colorColumn = "whiteUsername";
        }
        if(Objects.equals(game.blackUsername(), username)) {
            colorColumn = "blackUsername";
        }
        gameDAO.removePlayer(gameID, colorColumn);
        String leaveMessage = String.format("%s has left the game\n", username);
        ServerMessage leaveNotice = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, leaveMessage);
        try {
            connections.broadcast(gameID, username, leaveNotice);
        } catch(IOException e) {}
    }

}
