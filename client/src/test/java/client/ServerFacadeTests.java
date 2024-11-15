package client;

import chess.ChessGame;
import dataaccess.GameDAO;
import records.*;
import dataaccess.SQLUserDAO;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static SQLGameDAO gameDAO = new SQLGameDAO();
    private static SQLAuthDAO authDAO = new SQLAuthDAO();
    private static SQLUserDAO userDAO = new SQLUserDAO();

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
        gameDAO.clearData();
        authDAO.clearData();
        userDAO.clearData();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    void registerPositive() throws ResponseException {
        UserData newUser = new UserData("user", "pass", "email");
        facade.register(newUser);
        assertTrue(!facade.getAuth().isEmpty());
    }
    @Test
    @Order(2)
    void createPositive() throws ResponseException {
        String gameName = "new_game";
        facade.create(gameName);
        GameData expectedGame = new GameData(1, null, null, "new_game", new ChessGame());
        assertFalse(()->gameDAO.listGames().isEmpty());
    }
    @Test
    @Order(3)
    void listPositive() throws ResponseException {
        AbbreviatedGameData gameData = new AbbreviatedGameData(1, null, null, "new_game");
        ArrayList<AbbreviatedGameData> expectedList = new ArrayList<>();
        expectedList.add(gameData);
        assertEquals(expectedList, facade.list());
    }
    @Test
    @Order(4)
    void joinPositive() throws ResponseException {
        assertDoesNotThrow(()->facade.join(1, ChessGame.TeamColor.WHITE));
    }
    @Test
    @Order(5)
    void logoutPositive() throws ResponseException {
        assertDoesNotThrow(()->facade.logout());
    }
    @Test
    @Order(6)
    void logoutNegative() throws ResponseException {
        assertThrows(ResponseException.class, ()->facade.logout());
    }
    @Test
    @Order(7)
    void loginNegative() throws ResponseException {
        UserData badUser = new UserData("wrong", "wrong", null);
        assertThrows(ResponseException.class, ()->facade.login(badUser));
    }
    @Test
    @Order(8)
    void registerNegative() throws ResponseException {
        UserData takenName = new UserData("user", "aPassword", "anEmail");
        assertThrows(ResponseException.class, ()->facade.register(takenName));
    }
    @Test
    @Order(9)
    void listNegative() throws ResponseException {
        assertThrows(ResponseException.class, ()->facade.list());
    }
    @Test
    @Order(10)
    void loginPositive() throws ResponseException {
        UserData goodLogin = new UserData("user", "pass", null);
        assertDoesNotThrow(()->facade.login(goodLogin));
    }
    @Test
    @Order(11)
    void joinNegative() throws records.ResponseException {
        assertThrows(RuntimeException.class, ()->facade.join(2, ChessGame.TeamColor.WHITE));
        facade.logout();
    }
    @Test
    @Order(12)
    void createNegative() throws ResponseException {
        assertThrows(ResponseException.class, ()->facade.create("game2"));
    }
}
