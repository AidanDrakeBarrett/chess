package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class ClearAppService {
    private static MemoryAuthDAO authDAO = new MemoryAuthDAO();
    private static MemoryGameDAO gameDAO = new MemoryGameDAO();
    private static MemoryUserDAO userDAO = new MemoryUserDAO();

    public ClearAppService() {}
    public void clearApplication() {
        authDAO.clearData();
        gameDAO.clearData();
        userDAO.clearData();
    }
}
