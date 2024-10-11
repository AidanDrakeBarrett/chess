package dataAccess;

public interface AuthDAO {
    void clearData();
    boolean containsAuth(String userAuth) throws DataAccessException;
}
