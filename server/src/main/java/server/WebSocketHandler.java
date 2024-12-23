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

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {
    private final ConnectionHandler connections = new ConnectionHandler();
    SQLAuthDAO authDAO = new SQLAuthDAO();
    SQLGameDAO gameDAO = new SQLGameDAO();
    /*public enum NullDataType {
        BAD_GAME,
        BAD_USER_AUTH
    }*/

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch(command.getCommandType()) {
            case CONNECT -> connect(session, command.getAuthToken(), command.getGameID());
            case MAKE_MOVE -> makeMove(session, command.getAuthToken(), command.getGameID(), command.getMove());
            case RESIGN -> resign(session, command.getAuthToken(), command.getGameID());
            case LEAVE -> leave(session, command.getAuthToken(), command.getGameID());
        }
    }
    private void connect(Session session, String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        if(gateKeep(session, game, username, gameID)) {
            return;
        }
        String black = game.blackUsername();
        String white = game.whiteUsername();
        ChessGame chessGame = game.chessGame();
        String chessGameString = new Gson().toJson(chessGame);

        connections.add(gameID, username, session);
        String userJoined = null;
        if(black != null && username.equals(black)) {
            userJoined = String.format("%s joined the game as black\n", username);
        }
        if(white != null && username.equals(white)) {
            userJoined = String.format("%s joined the game as white\n", username);
        }
        if(userJoined == null) {
            userJoined = String.format("%s joined the game as an observer\n", username);
        }
        ServerMessage joinNotice = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, userJoined);
        ServerMessage gameLoad = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, chessGameString);
        try {
            connections.broadcast(gameID, username, joinNotice);
            connections.sendToOne(username, gameLoad);
        } catch(IOException e) {}
    }
    private void makeMove(Session session, String authToken, int gameID, ChessMove move) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        if(gateKeep(session, game, username, gameID)) {
            return;
        }
        if(gameplayGateKeep(session, game, username)) {
            return;
        }

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
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            error.setErrorMessage("Error: move is invalid");
            try {
                connections.sendToOne(username, error);
            } catch(IOException ex) {}
            return;
        }

        GameData updatedGame = gameDAO.getGame(gameID);
        String whiteUser = updatedGame.whiteUsername();
        String blackUser = updatedGame.blackUsername();
        ChessGame chessGame = updatedGame.chessGame();
        String chessGameString = new Gson().toJson(chessGame);
        ServerMessage gameUpdate = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, chessGameString);

        StringBuilder moveMessage = new StringBuilder(String.format("%s moved %s from %s to %s", username, movedPiece,
                startingString, endingString));
        if(move.getPromotionPiece() != null) {
            moveMessage.append(String.format(" and promoted it to %s", pieceName(move.getPromotionPiece())));
        }
        moveMessage.append(".\n");
        StringBuilder aftermath = new StringBuilder();
        if(wCheckmate) {
            aftermath.append(String.format("%s/white is in checkmate.\n", whiteUser));
            gameDAO.endGame(gameID);
        }
        if(bCheckmate) {
            aftermath.append(String.format("%s/black is in checkmate.\n", blackUser));
            gameDAO.endGame(gameID);
        }
        if(!wCheckmate && !bCheckmate) {
            if(wCheck) {
                aftermath.append(String.format("%s/white is in check.\n", whiteUser));
            }
            if(bCheck) {
                aftermath.append(String.format("%s/black is in check.\n", blackUser));
            }
            if(wStalemate && bStalemate) {
                aftermath.append("The game is in stalemate.\n");
                gameDAO.endGame(gameID);
            }
        }
        ServerMessage announceMoveMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                moveMessage.toString());

        try {
            connections.broadcast(gameID, null, gameUpdate);
            connections.broadcast(gameID, username, announceMoveMessage);
            if(!aftermath.isEmpty()) {
                ServerMessage aftermathMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        aftermath.toString());
                connections.broadcast(gameID, null, aftermathMessage);
            }
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
    private void resign(Session session, String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        if(gateKeep(session, game, username, gameID)) {
            return;
        }
        if(!game.isActive()) {
            String message = "The game is over. Go home, bro.\n";
            badUser(session, message);
            return;
        }
        if(!Objects.equals(game.blackUsername(), username) && !Objects.equals(game.whiteUsername(), username)) {
            String message = "Sit DOWN! You are NOT a player.\n";
            badUser(session, message);
            return;
        }
        String letterOfRes = String.format("%s has resigned\n", username);
        String acceptRes = "You have resigned\n";
        ServerMessage resignation = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, letterOfRes);
        ServerMessage acceptance = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, acceptRes);
        gameDAO.endGame(gameID);
        try {
            connections.broadcast(gameID, username, resignation);
            connections.sendToOne(username, acceptance);
        } catch(IOException e) {}
    }
    private void leave(Session session, String authToken, int gameID) {
        String username = authDAO.getUsername(authToken);
        GameData game = gameDAO.getGame(gameID);
        if(gateKeep(session, game, username, gameID)) {
            return;
        }
        String colorColumn = null;
        if(Objects.equals(game.whiteUsername(), username)) {
            colorColumn = "whiteUsername";
        }
        if(Objects.equals(game.blackUsername(), username)) {
            colorColumn = "blackUsername";
        }
        gameDAO.removePlayer(gameID, colorColumn);
        connections.remove(username);
        String leaveMessage = String.format("%s has left the game\n", username);
        ServerMessage leaveNotice = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, leaveMessage);
        try {
            connections.broadcast(gameID, username, leaveNotice);
        } catch(IOException e) {}

    }
    private boolean gateKeep(Session session, GameData game, String username, int gameID) {
        if(username == null) {
            String message = "Error: unauthorized\n";
            badUser(session, message);
            return true;
        }
        if(game == null) {
            badGameID(session, gameID);
            return true;
        }
        return false;
    }
    private boolean gameplayGateKeep(Session session, GameData game, String username) {
        if(!game.isActive()) {
            String message = "The game is over. Go home, bro.\n";
            badUser(session, message);
            return true;
        }
        if(!Objects.equals(game.blackUsername(), username) && !Objects.equals(game.whiteUsername(), username)) {
            String message = "Sit DOWN! You are NOT a player.\n";
            badUser(session, message);
            return true;
        }
        ChessGame.TeamColor whoseTurn = game.chessGame().getTeamTurn();
        if(Objects.equals(username, game.whiteUsername())) {
            if(whoseTurn != ChessGame.TeamColor.WHITE) {
                String message = "Wait your turn\n";
                badUser(session, message);
                return true;
            }
        }
        if(Objects.equals(username, game.blackUsername())) {
            if(whoseTurn != ChessGame.TeamColor.BLACK) {
                String message = "Wait your turn\n";
                badUser(session, message);
                return true;
            }
        }
        return false;
    }
    private void badGameID(Session session, int gameID) {
        String errorMsg = String.format("Error: %d is not a valid game number\n", gameID);
        ServerMessage gameIDError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        gameIDError.setErrorMessage(errorMsg);
        try {
            connections.sendBadAuthOrID(session, gameIDError);
        } catch(IOException e) {}
    }
    private void badUser(Session session, String errorMsg) {
        ServerMessage userAuthError = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        userAuthError.setErrorMessage(errorMsg);
        try {
            connections.sendBadAuthOrID(session, userAuthError);
        } catch(IOException e) {}
    }
}
