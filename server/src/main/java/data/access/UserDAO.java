package data.access;

public interface UserDAO {
    void clearData();
    boolean containsUsername(String username) throws DataAccessException;
    boolean getLogin(UserData login) throws DataAccessException;
    void createUser(UserData newUser);
}
