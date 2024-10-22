package server;

import chess.ChessGame;

public record JoinRequests(ChessGame.TeamColor playerColor, int gameID) {
}
