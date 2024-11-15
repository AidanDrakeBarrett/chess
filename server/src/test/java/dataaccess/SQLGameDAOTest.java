package dataaccess;

import chess.ChessGame;
import records.GameData;
import records.AbbreviatedGameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {
    SQLGameDAO gameDAO = new SQLGameDAO();
    String gameName1 = "gameName1";
    ChessGame game1 = new ChessGame();
    GameData gameData1 = new GameData(1, null, null, gameName1, game1);

    @AfterEach
    void tearDown() {
        gameDAO.clearData();
    }
    @Test
    void createGamePositive() {
        int gameID1 = gameDAO.createGame(gameName1);
        assertEquals(gameData1.gameID(), gameID1);
        assertTrue(gameDAO.getGame(gameID1).chessGame().getBoard().equals(gameData1.chessGame().getBoard()));
    }
    @Test
    void createGameNegative() {
        gameDAO.createGame(gameName1);
        int gameID2 = gameDAO.createGame(gameName1);
        assertNotEquals(gameData1.gameID(), gameID2);
    }
    @Test
    void clearData() {
        gameDAO.createGame(gameName1);
        gameDAO.clearData();
        ArrayList<AbbreviatedGameData> emptyList = new ArrayList<>();
        assertEquals(emptyList, gameDAO.listGames());
    }
    @Test
    void getGamePositive() {
        int gameID1 = gameDAO.createGame(gameName1);
        assertTrue(gameDAO.getGame(gameID1).chessGame().getBoard().equals(gameData1.chessGame().getBoard()));
    }
    @Test
    void getGameNegative() {
        int gameID1 = gameDAO.createGame(gameName1);
        AbbreviatedGameData abbreviation = new AbbreviatedGameData(gameID1, null, null, gameName1);
        assertEquals(abbreviation, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()).getFirst());
        assertNull(gameDAO.getGame(2));
    }

    @Test
    void joinGamePlayerPositive() throws DataAccessException {
        int gameID1 = gameDAO.createGame(gameName1);
        String username = "username";
        gameDAO.joinGame(username, ChessGame.TeamColor.WHITE, gameID1);
        AbbreviatedGameData abbreviation = new AbbreviatedGameData(gameID1, username, null, gameName1);
        assertEquals(abbreviation, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()).getFirst());
    }
    @Test
    void joinGamePlayerNegative() throws DataAccessException {
        int gameID1 = gameDAO.createGame(gameName1);
        String username1 = "username1";
        gameDAO.joinGame(username1, ChessGame.TeamColor.WHITE, gameID1);
        AbbreviatedGameData abbreviation = new AbbreviatedGameData(gameID1, username1, null, gameName1);
        assertEquals(abbreviation, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()).getFirst());
        String username2 = "username2";
        assertThrows(DataAccessException.class, ()->gameDAO.joinGame(username2, ChessGame.TeamColor.WHITE, gameID1));
        assertEquals(abbreviation, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()).getFirst());
    }
    @Test
    void listGamesPositive() {
        int gameID1 = gameDAO.createGame(gameName1);
        String gameName2 = "gameName2";
        ChessGame game2 = new ChessGame();
        HashSet<String> spectators2 = new HashSet<>();
        GameData gameData2 = new GameData(2, null, null, gameName2, game2);
        int gameID2 = gameDAO.createGame(gameName2);
        AbbreviatedGameData abbreviation1 = new AbbreviatedGameData(gameID1, null, null, gameName1);
        AbbreviatedGameData abbreviation2 = new AbbreviatedGameData(gameID2, null, null, gameName2);
        ArrayList<AbbreviatedGameData> abbreviations = new ArrayList<>();
        abbreviations.add(abbreviation1);
        abbreviations.add(abbreviation2);
        assertEquals(abbreviations, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()));
    }
    @Test
    void listGamesNegative() {
        ArrayList<AbbreviatedGameData> fakeGames = new ArrayList<>();
        AbbreviatedGameData fakeGame = new AbbreviatedGameData(gameData1.gameID(), null, null, gameName1);
        fakeGames.add(fakeGame);
        assertNotEquals(fakeGames, gameDAO.listGames());
    }
}
