package records;

import chess.ChessGame;

import java.util.HashSet;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame chessGame,
                       boolean isActive) {
}
