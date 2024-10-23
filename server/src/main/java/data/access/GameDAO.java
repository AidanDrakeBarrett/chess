package data.access;

import chess.ChessGame;

import java.util.Collection;

public interface GameDAO {
    void clearData();
    GameData getGame(int gameID);
    void joinGame(String username, ChessGame.TeamColor clientColor, int gameID) throws DataAccessException;
    int createGame(String gameName);
    Collection<AbbreviatedGameData> listGames();
}
