package service;

import chess.ChessGame;
import records.AbbreviatedGameData;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import records.ResponseException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GamesServiceTest {
    private final GamesService service = new GamesService();
    private final SQLGameDAO gameDAO = new SQLGameDAO();
    private final SQLAuthDAO authDAO = new SQLAuthDAO();
    private final String username1 = "user1";
    private final String username2 = "user2";
    private final String gameName1 = "game1";
    private final String gameName2 = "game2";
    private String authToken1;
    private String authToken2;

    @BeforeEach
    void setUp() {
        authToken1 = authDAO.createAuth(username1).authToken();
        authToken2 = authDAO.createAuth(username2).authToken();
    }
    @AfterEach
    void tearDown() {
        gameDAO.clearData();
        authDAO.clearData();
    }

    @Test
    void listGamesPositive() throws ResponseException {
        int id = gameDAO.createGame(gameName1);
        AbbreviatedGameData abbreviatedGameData = new AbbreviatedGameData(id, null, null, gameName1);
        ArrayList<AbbreviatedGameData> abbreviations = new ArrayList<>();
        abbreviations.add(abbreviatedGameData);
        assertEquals(abbreviations, GamesService.listGames(authToken1));
    }
    @Test
    void listGamesNegative() throws ResponseException {
        int id = gameDAO.createGame(gameName1);
        AbbreviatedGameData abbreviatedGameData = new AbbreviatedGameData(id, null, null, gameName1);
        ArrayList<AbbreviatedGameData> abbreviations = new ArrayList<>();
        abbreviations.add(abbreviatedGameData);
        assertThrows(ResponseException.class, ()->GamesService.listGames("username"));
    }
    @Test
    void createGamePositive() {
        try {
            assertEquals(1, GamesService.createGame(authToken1, gameName1));
        } catch(ResponseException resEx) {
            fail("Caught unexpected response exception");
        }
    }
    @Test
    void createGameNegative() {
        try {
            int id1 = (int) GamesService.createGame(authToken1, gameName1);
            assertNotEquals(id1, GamesService.createGame(authToken1, gameName2));
        } catch(ResponseException resEx) {
            fail("Caught unexpected response exception");
        }
    }
    @Test
    void joinGamePositive() throws ResponseException {
        int gameID = gameDAO.createGame(gameName1);
        service.joinGame(authToken1, ChessGame.TeamColor.WHITE, gameID);
        assertEquals(username1, ((ArrayList<AbbreviatedGameData>) gameDAO.listGames()).getFirst().whiteUsername());
        assertThrows(ResponseException.class, ()->service.joinGame(authToken2, ChessGame.TeamColor.WHITE, gameID));
    }
    @Test
    void joinGameNegative() throws ResponseException {
        int gameID = gameDAO.createGame(gameName1);
        service.joinGame(authToken1, ChessGame.TeamColor.WHITE, gameID);
        assertThrows(ResponseException.class, ()->service.joinGame(authToken2, ChessGame.TeamColor.WHITE, gameID));
    }
}
