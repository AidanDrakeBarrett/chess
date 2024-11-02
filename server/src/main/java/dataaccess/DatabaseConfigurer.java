package dataaccess;

import server.ResponseException;

import java.sql.SQLException;

public class DatabaseConfigurer {
    public DatabaseConfigurer() {}
    public static void configureDatabase(String[] createStatements) throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try(var conn = DatabaseManager.getConnection()) {
            for(var statement:createStatements) {
                try(var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch(SQLException e) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", e.getMessage()));
        }
    }
}
