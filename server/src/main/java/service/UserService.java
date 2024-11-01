package service;

import dataaccess.*;
import server.ResponseException;

public class UserService {
    private static MemoryUserDAO userDAO = new MemoryUserDAO();
    private static MemoryAuthDAO authDAO = new MemoryAuthDAO();

    public UserService() {}
    public AuthData register(UserData newUser) throws ResponseException {
        try {
            userDAO.containsUsername(newUser.username());
        } catch(DataAccessException e) {
            throw new ResponseException(403, "already taken");
        }
        userDAO.createUser(newUser);
        return authDAO.createAuth(newUser.username());
    }
    public AuthData login(UserData userLogin) throws ResponseException {
        try {
            if(userDAO.getLogin(userLogin)) {
                return authDAO.createAuth(userLogin.username());
            }
        } catch(DataAccessException e) {
            throw new ResponseException(401, "unauthorized");
        }
        return null;
    }
    public void logout(String authToken) throws ResponseException {
        try {
            if(authDAO.containsAuth(authToken)) {
                authDAO.deleteAuth(authToken);
            }
        } catch(DataAccessException e) {
            throw new ResponseException(401, "unauthorized");
        }
    }
}
