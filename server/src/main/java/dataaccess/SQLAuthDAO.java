package dataaccess;

import server.ResponseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class SQLAuthDAO implements AuthDAO {
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS AuthData (
                username VARCHAR(255) NOT NULL,
                authToken VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
            );
            """
    };
    public SQLAuthDAO() {
        try {
            DatabaseConfigurer.configureDatabase(createStatements);
        } catch(ResponseException resEx) {}
        catch(DataAccessException e) {}
    }
    @Override
    public void clearData() {
        try(var conn = DatabaseManager.getConnection()) {
            try(var preparedStatement = conn.prepareStatement("TRUNCATE TABLE AuthData;")) {
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch (DataAccessException e) {}
    }

    @Override
    public boolean containsAuth(String userAuth) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT * FROM AuthData
                    WHERE authToken = ?;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, userAuth);
                try(var rs = preparedStatement.executeQuery()) {
                    return authResultSetProcessing(rs, userAuth);
                } catch(SQLException e) {
                    throw new DataAccessException("");
                }
            } catch(SQLException e) {
                throw new DataAccessException("");
            }
        } catch(SQLException e) {
            throw new DataAccessException("Error: unauthorized");
        }
    }
    private boolean authResultSetProcessing(ResultSet rs, String userAuth) throws DataAccessException, SQLException {
        while(rs.next()) {
            if(Objects.equals(rs.getString("authToken"), userAuth)) {
                return true;
            }
        }
        throw new DataAccessException("Error: unauthorized");
    }

    @Override
    public void deleteAuth(String authToken) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    DELETE FROM AuthData
                    WHERE authToken = ?;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch (DataAccessException e) {}
    }

    @Override
    public AuthData createAuth(String username) {
        String authToken = UUID.randomUUID().toString().replace("-","");
        AuthData newAuth = new AuthData(username, authToken);
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    INSERT INTO AuthData (username, authToken)
                    VALUES (?, ?);
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, authToken);
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch(DataAccessException e) {}
        return newAuth;
    }

    @Override
    public String getUsername(String authToken) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT username FROM AuthData
                    WHERE authToken = ?;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, authToken);
                try(var rs = preparedStatement.executeQuery()) {
                    String authorizedUser = null;
                    while(rs.next()) {
                        authorizedUser = rs.getString("username");
                    }
                    return authorizedUser;
                } catch(SQLException e) {}
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
        return null;
    }
}
