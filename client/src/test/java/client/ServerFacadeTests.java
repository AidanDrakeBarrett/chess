package client;

import chess.ChessGame;
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
    void registerPositive() {
        UserData newUser = new UserData("user", "pass", "email");
        facade.register(newUser);
        assertTrue(!facade.getAuth().isEmpty());
    }
    @Test
    @Order(2)
    void createPositive() {
        String gameName = "new_game";
        facade.create(gameName);
        GameData expectedGame = new GameData(1, null, null, "new_game", new ChessGame(), true);
        assertFalse(()->gameDAO.listGames().isEmpty());
    }
    @Test
    @Order(3)
    void listPositive() {
        AbbreviatedGameData gameData = new AbbreviatedGameData(1, null, null, "new_game");
        ArrayList<AbbreviatedGameData> expectedList = new ArrayList<>();
        expectedList.add(gameData);
        assertEquals(expectedList, facade.list());
    }
    @Test
    @Order(4)
    void joinPositive() {
        assertDoesNotThrow(()->facade.join(1, ChessGame.TeamColor.WHITE));
    }
    @Test
    @Order(5)
    void logoutPositive() {
        assertDoesNotThrow(()->facade.logout());
    }
    @Test
    @Order(6)
    void logoutNegative() {
        assertThrows(RuntimeException.class, ()->facade.logout());
    }
    @Test
    @Order(7)
    void loginNegative() {
        UserData badUser = new UserData("wrong", "wrong", null);
        assertThrows(RuntimeException.class, ()->facade.login(badUser));
    }
    @Test
    @Order(8)
    void registerNegative() {
        UserData takenName = new UserData("user", "aPassword", "anEmail");
        assertThrows(RuntimeException.class, ()->facade.register(takenName));
    }
    @Test
    @Order(9)
    void listNegative() {
        assertThrows(RuntimeException.class, ()->facade.list());
    }
    @Test
    @Order(10)
    void loginPositive() {
        UserData goodLogin = new UserData("user", "pass", null);
        assertDoesNotThrow(()->facade.login(goodLogin));
    }
    @Test
    @Order(11)
    void joinNegative() {
        assertThrows(RuntimeException.class, ()->facade.join(2, ChessGame.TeamColor.WHITE));
        facade.logout();
    }
    @Test
    @Order(12)
    void createNegative() {
        assertThrows(RuntimeException.class, ()->facade.create("game2"));
    }
}
