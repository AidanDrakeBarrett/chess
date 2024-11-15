package dataaccess;

import records.AuthData;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private static HashSet<AuthData> authDataHashSet = new HashSet<>();
    @Override
    public void clearData() {
        authDataHashSet.clear();;
    }

    @Override
    public boolean containsAuth(String userAuth) throws DataAccessException {
        for(AuthData auth:authDataHashSet) {
            if(auth.authToken().equals(userAuth)) {
                return true;
            }
        }
        throw new DataAccessException("Error: unauthorized");
    }

    @Override
    public void deleteAuth(String authToken) {
        authDataHashSet.removeIf(auth -> Objects.equals(auth.authToken(), authToken));
    }

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString().replace("-", "");
        AuthData newAuth = new AuthData(username, authToken);
        authDataHashSet.add(newAuth);
        return newAuth;
    }

    @Override
    public String getUsername(String authToken) {
        for(AuthData auth:authDataHashSet) {
            if(Objects.equals(auth.authToken(), authToken)) {
                return auth.username();
            }
        }
        return null;
    }

    //FOR TESTING
    public static HashSet<AuthData> getAuthDataHashSet() {
        return authDataHashSet;
    }
}
