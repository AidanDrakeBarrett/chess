package service;

import data.access.*;
import dataAccess.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class ClearAppServiceTest {
    private ClearAppService service = new ClearAppService();
    private MemoryGameDAO gameDAO = new MemoryGameDAO();
    private MemoryAuthDAO authDAO = new MemoryAuthDAO();
    private MemoryUserDAO userDAO = new MemoryUserDAO();

    @Test
    void clearApplication() {
        String gameName = "game";
        gameDAO.createGame(gameName);

        String username = "username";
        String password = "password";
        String email = "email";
        UserData newUser = new UserData(username, password, email);
        userDAO.createUser(newUser);

        String authToken = authDAO.createAuth(username).authToken();

        service.clearApplication();
        assertTrue(gameDAO.listGames().isEmpty());
        assertThrows(DataAccessException.class, ()->userDAO.getLogin(newUser));
        assertNull(authDAO.getUsername(authToken));
    }
}
