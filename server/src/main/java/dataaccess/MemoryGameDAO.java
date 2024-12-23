package dataaccess;

import chess.ChessGame;
import records.GameData;
import records.AbbreviatedGameData;

import java.util.ArrayList;
import java.util.Collection;

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
                    String black = game.blackUsername();
                    String gName = game.gameName();
                    GameData newGame = new GameData(gameID, username, black, gName, game.chessGame(), true);
                    gameDataArrayList.remove(game);
                    gameDataArrayList.add(newGame);
                    return;
                }
                if(clientColor == ChessGame.TeamColor.BLACK) {
                    if(game.blackUsername() != null) {
                        throw new DataAccessException("Error: already taken");
                    }
                    String white = game.whiteUsername();
                    String gName = game.gameName();
                    GameData newGame = new GameData(gameID, white, username, gName, game.chessGame(), true);
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
        GameData newGameData = new GameData(gameID, null, null, gameName, newGame, true);
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
