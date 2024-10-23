package dataAccess;

import chess.ChessGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {
    private static ArrayList<GameData> gameDataArrayList = new ArrayList<>();
    @Override
    public void clearData() {
        gameDataArrayList.clear();;
    }

    @Override
    public GameData getGame(int gameID) {
        for(GameData game:gameDataArrayList) {
            if(game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public void joinGame(String username, ChessGame.TeamColor clientColor, int gameID) throws DataAccessException {
        for(GameData game:gameDataArrayList) {
            if(game.gameID() == gameID) {
                if(clientColor == ChessGame.TeamColor.WHITE) {
                    if(game.whiteUsername() != null) {
                        throw new DataAccessException("Error: already taken");
                    }
                    GameData newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.chessGame(), game.spectators());
                    gameDataArrayList.remove(game);
                    gameDataArrayList.add(newGame);
                    return;
                }
                if(clientColor == ChessGame.TeamColor.BLACK) {
                    if(game.blackUsername() != null) {
                        throw new DataAccessException("Error: already taken");
                    }
                    GameData newGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.chessGame(), game.spectators());
                    gameDataArrayList.remove(game);
                    gameDataArrayList.add(newGame);
                    return;
                }
            }
        }
        throw new DataAccessException("Error: bad request");
    }

    @Override
    public int createGame(String gameName) {
        int gameID = gameDataArrayList.size() + 1;
        ChessGame newGame = new ChessGame();
        GameData newGameData = new GameData(gameID, null, null, gameName, newGame, new HashSet<>());
        gameDataArrayList.add(newGameData);
        return gameID;
    }

    @Override
    public Collection<AbbreviatedGameData> listGames() {
        ArrayList<AbbreviatedGameData> games = new ArrayList<>();
        for(GameData game:gameDataArrayList) {
            AbbreviatedGameData smallGame = new AbbreviatedGameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName());
            games.add(smallGame);
        }
        return games;
    }
}
