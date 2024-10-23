package service;

import data.access.MemoryAuthDAO;
import data.access.MemoryGameDAO;
import data.access.MemoryUserDAO;

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
