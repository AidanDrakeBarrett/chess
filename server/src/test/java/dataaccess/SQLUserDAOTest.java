package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.SQLUserDAO;
import dataaccess.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {
    String username1 = "username1";
    String password1 = "password1";
    String email1 = "email1";
    UserData user1 = new UserData(username1, password1, email1);
    SQLUserDAO userDAO = new SQLUserDAO();
    @AfterEach
    void tearDown() {
        userDAO.clearData();
    }
    @Test
    void createUserPositive() throws DataAccessException {
        userDAO.createUser(user1);
        assertTrue(userDAO.getLogin(user1));
    }
    @Test
    void createUserNegative() throws DataAccessException {
        userDAO.createUser(user1);
        assertTrue(userDAO.getLogin(user1));
        String newPass = "newPass";
        String newEmail = "newEmail";
        UserData badUser = new UserData(username1, newPass, newEmail);
        userDAO.createUser(badUser);
        assertThrows(DataAccessException.class, ()->userDAO.getLogin(badUser));
    }
    @Test
    void clearData() throws DataAccessException {
        userDAO.createUser(user1);
        assertTrue(userDAO.getLogin(user1));
        userDAO.clearData();
        assertThrows(DataAccessException.class, ()->userDAO.getLogin(user1));
    }
    @Test
    void containsUsernamePositive() throws DataAccessException {
        userDAO.createUser(user1);
        assertTrue(userDAO.getLogin(user1));
        assertThrows(DataAccessException.class, ()->userDAO.containsUsername(user1.username()));
    }
    @Test
    void containsUsernameNegative() throws DataAccessException {
        userDAO.createUser(user1);
        assertTrue(userDAO.getLogin(user1));
        userDAO.clearData();
        assertFalse(userDAO.containsUsername(user1.username()));
    }
    @Test
    void getLoginPositive() throws DataAccessException {
        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, ()->userDAO.containsUsername(user1.username()));
        assertTrue(userDAO.getLogin(user1));
    }
    @Test
    void getLoginNegative() throws DataAccessException {
        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, ()->userDAO.containsUsername(user1.username()));
        userDAO.clearData();
        assertThrows(DataAccessException.class, ()->userDAO.getLogin(user1));
    }
}
