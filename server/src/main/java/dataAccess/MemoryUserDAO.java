package dataAccess;

import java.util.HashSet;
import java.util.Objects;

public class MemoryUserDAO implements UserDAO {
    private static HashSet<UserData> userDataHashSet = new HashSet<>();
    @Override
    public void clearData() {
        userDataHashSet.clear();
    }

    @Override
    public boolean containsUsername(String username) throws DataAccessException {
        for(UserData user:userDataHashSet) {
            if(Objects.equals(user.username(), username)) {
                throw new DataAccessException("");
            }
        }
        return false;
    }

    @Override
    public boolean getLogin(UserData login) throws DataAccessException {
        for(UserData user:userDataHashSet) {
            if(Objects.equals(user.username(), login.username())) {
                if(Objects.equals(user.password(), login.password())) {
                    return true;
                }
                throw new DataAccessException("unauthorized");
            }
        }
        throw new DataAccessException("unauthorized");
    }

    @Override
    public void createUser(UserData newUser) {
        userDataHashSet.add(newUser);
    }
    //FOR TESTING
    public HashSet<UserData> getUserDataHashSet() {
        return userDataHashSet;
    }
}
