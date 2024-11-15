package dataaccess;

import org.mindrot.jbcrypt.BCrypt;
import server.ResponseException;

import java.sql.*;
import java.util.Objects;

public class SQLUserDAO implements UserDAO {
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS UserData(
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
            );
            """
    };
    public SQLUserDAO() {
        try {
            DatabaseManager.configureDatabase(createStatements);
        } catch(ResponseException e) {}
        catch(DataAccessException e) {}
    }
    @Override
    public void clearData() {
        try(var conn = DatabaseManager.getConnection()) {
            try(var preparedStatement = conn.prepareStatement("TRUNCATE TABLE UserData;")) {
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch(DataAccessException e) {}
    }

    @Override
    public boolean containsUsername(String username) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT * FROM UserData
                    WHERE username = ?;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try(var rs = preparedStatement.executeQuery()) {
                    if(!rs.next()) {
                        return false;
                    }
                    if(Objects.equals(rs.getString("username"), username)) {
                        throw new DataAccessException("");
                    }
                } catch(SQLException e) {
                    throw new DataAccessException("");
                }
            } catch(SQLException e) {
                throw new DataAccessException("");
            }
        } catch(SQLException e) {
            throw new DataAccessException("");
        }
        return false;
    }

    @Override
    public boolean getLogin(UserData login) throws DataAccessException {
        String username = login.username();
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT password FROM UserData
                    WHERE username = ?;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                try(var rs = preparedStatement.executeQuery()) {
                    String queriedPassword = null;
                    if(rs.next()) {
                        queriedPassword = rs.getString("password");
                    }
                    if(queriedPassword == null) {
                        throw new DataAccessException("");
                    }
                    if(BCrypt.checkpw(login.password(), queriedPassword)) {
                        return true;
                    }
                } catch(SQLException e) {
                    throw new DataAccessException("");
                }
            } catch(SQLException e) {
                throw new DataAccessException("");
            }
        } catch(SQLException e) {
            throw new DataAccessException("");
        }
        throw new DataAccessException("unauthorized");
    }

    @Override
    public void createUser(UserData newUser) {
        String username = newUser.username();
        String hashedPassword = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        String email = newUser.email();
        var statement = """
                INSERT INTO UserData
                (username, password, email)
                VALUES (?, ?, ?);
                """;
        try(var conn = DatabaseManager.getConnection()) {
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, email);
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
    }
}
