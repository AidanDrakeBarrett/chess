package service;

import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLUserDAO;
import records.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import records.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private final UserService service = new UserService();
    private final SQLUserDAO userDAO = new SQLUserDAO();
    private final SQLAuthDAO authDAO = new SQLAuthDAO();
    private final String username1 = "user1";
    private final String username2 = "user2";
    private final String password1 = "password1";
    private final String password2 = "password2";
    private final String email1 = "email1";
    private final String email2 = "email2";

    @AfterEach
    void tearDown() {
        userDAO.clearData();
        authDAO.clearData();
    }

    @Test
    void registerPositive() throws ResponseException, DataAccessException {
        UserData user1 = new UserData(username1, password1, email1);
        service.register(user1);
        assertTrue(userDAO.getLogin(user1));
    }
    @Test
    void registerNegative() throws ResponseException {
        UserData user1 = new UserData(username1, password1, email1);
        service.register(user1);
        UserData user2 = new UserData(username1, password2, email2);
        assertThrows(ResponseException.class, ()->service.register(user2));
    }
    @Test
    void loginPositive() throws ResponseException, DataAccessException {
        UserData user1 = new UserData(username1, password1, email1);
        userDAO.createUser(user1);
        String userAuth = service.login(user1).authToken();
        assertTrue(authDAO.containsAuth(userAuth));
    }
    @Test
    void loginNegative() {
        UserData user1 = new UserData(username1, password1, email1);
        userDAO.createUser(user1);
        UserData user2 = new UserData(username2, password1, email1);
        assertThrows(ResponseException.class, ()->service.login(user2));
    }
    @Test
    void logoutPositive() throws ResponseException, DataAccessException {
        String authToken = authDAO.createAuth(username1).authToken();
        assertTrue(authDAO.containsAuth(authToken));
        service.logout(authToken);
        assertThrows(DataAccessException.class, ()->authDAO.containsAuth(authToken));
    }
    @Test
    void logoutNegative() throws ResponseException {
        assertThrows(ResponseException.class, ()->service.logout("faketoken"));
    }
}
