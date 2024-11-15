package dataaccess;

import chess.ChessGame;
import records.GameData;
import records.AbbreviatedGameData;

import java.util.Collection;

public interface GameDAO {
    void clearData();
    GameData getGame(int gameID);
    void joinGame(String username, ChessGame.TeamColor clientColor, int gameID) throws DataAccessException;
    int createGame(String gameName);
    Collection<AbbreviatedGameData> listGames();
}
