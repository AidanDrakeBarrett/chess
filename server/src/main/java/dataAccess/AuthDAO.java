package dataAccess;

public interface AuthDAO {
    void clearData();
    boolean containsAuth(String userAuth) throws DataAccessException;
    void deleteAuth(String authToken);
    AuthData createAuth(String username);
    String getUsername(String authToken);
}
