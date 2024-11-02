package service;

import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.SQLUserDAO;

public class ClearAppService {
    private static SQLAuthDAO authDAO = new SQLAuthDAO();
    private static SQLGameDAO gameDAO = new SQLGameDAO();
    private static SQLUserDAO userDAO = new SQLUserDAO();

    public ClearAppService() {}
    public void clearApplication() {
        authDAO.clearData();
        gameDAO.clearData();
        userDAO.clearData();
    }
}
