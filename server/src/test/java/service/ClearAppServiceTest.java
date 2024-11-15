package service;

import dataaccess.*;
import records.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class ClearAppServiceTest {
    private ClearAppService service = new ClearAppService();
    private SQLGameDAO gameDAO = new SQLGameDAO();
    private SQLAuthDAO authDAO = new SQLAuthDAO();
    private SQLUserDAO userDAO = new SQLUserDAO();

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
