package dataAccess;

import chess.ChessGame;

import com.google.gson.Gson;
import server.ResponseException;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.HashSet;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;
public class SQLGameDAO implements GameDAO{
    public SQLGameDAO() {
        try {
            configureDatabase();
        } catch(ResponseException resEx) {}
        catch(DataAccessException e) {}
    }
    @Override
    public void clearData() {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                TRUNCATE TABLE GameData
                """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            } catch(SQLException sqlEx) {}
        } catch(SQLException sqlEx) {}
        catch(DataAccessException e) {}
    }

    @Override
    public GameData getGame(int gameID) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT * FROM GameData 
                    WHERE id = ?
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try(var rs = preparedStatement.executeQuery()) {
                    if(rs.next()) {
                        int id = rs.getInt("id");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        String gameJson = rs.getString("chessGame");
                        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);
                        String spectatorJson = rs.getString("spectators");
                        HashSet<String> spectators = new Gson().fromJson(spectatorJson, HashSet.class);
                        GameData returnGame = new GameData(id, whiteUsername, blackUsername, gameName, chessGame, spectators);
                        return returnGame;
                    }
                } catch(SQLException sqlEx) {}
            } catch(SQLException sqlEx) {}
        } catch(SQLException sqlEx) {}
        catch(DataAccessException e) {}
        return null;
    }

    @Override
    public void joinGame(String username, ChessGame.TeamColor clientColor, int gameID) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            String colorColumn = null;
            if(clientColor == ChessGame.TeamColor.WHITE) {
                colorColumn = """
                        whiteUsername
                        """;
            }
            if(clientColor == ChessGame.TeamColor.BLACK) {
                colorColumn = """
                        blackUsername
                        """;
            }
            if(clientColor == null) {
                colorColumn = """
                        spectators
                        """;
            }
            var statement = """
                    SELECT
                                                """
                    + colorColumn +
                    """
                     FROM GameData WHERE id = ?
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setInt(1, gameID);
                try(var rs = preparedStatement.executeQuery()) {
                    if(rs.next()) {
                        if((clientColor == ChessGame.TeamColor.WHITE) || (clientColor == ChessGame.TeamColor.BLACK)) {
                            if(!Objects.equals(rs.getString(colorColumn), null)) {
                                throw new DataAccessException("Error: already taken");
                            }
                            playerInserter(username, colorColumn, gameID, conn);
                            return;
                        }
                        if((clientColor == null)) {
                            spectatorInserter(username, gameID, conn);
                            return;
                        }
                    }
                } catch(SQLException sqlEx) {
                    throw new DataAccessException("Error: bad request");
                }
            } catch(SQLException sqlEx) {}
        } catch(SQLException sqlEx) {}
        catch(DataAccessException e) {}
    }

    private void playerInserter(String username, String colorColumn, int gameID, Connection conn) throws SQLException {
        var statement = """
                UPDATE GameData SET 
                """
                + colorColumn +
                """
 
                 = ? WHERE id = ?
                """;
        try(var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, gameID);
            preparedStatement.executeUpdate();
        }
    }
    private void spectatorInserter(String username, int gameID, Connection conn) throws SQLException {
        var statement = """
                SELECT spectators FROM GameData 
                WHERE id = ?
                """;
        try(var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setInt(1, gameID);
            try(var rs = preparedStatement.executeQuery()) {
                String spectatorJson = rs.getString("spectators");
                HashSet<String> spectators = new Gson().fromJson(spectatorJson, HashSet.class);
                spectators.add(username);
                var updatedSpectatorJson = new Gson().toJson(spectators);
                var updateStatement = """
                        UPDATE GameData 
                        SET spectators 
                        WHERE id = ?
                        """;
                try(var preparedUpdateStatement = conn.prepareStatement(updateStatement)) {
                    preparedUpdateStatement.setInt(1, gameID);
                    preparedUpdateStatement.executeUpdate();
                }
            }
        }
    }

    @Override
    public int createGame(String gameName) {
        try(var conn = DatabaseManager.getConnection()) {
            ChessGame game = new ChessGame();
            var gameJson = new Gson().toJson(game);
            HashSet<String> spectators = new HashSet<>();
            var spectatorJson = new Gson().toJson(spectators);
            var statement = """
                    INSERT INTO GameData 
                    (gameName, chessGame, spectators) 
                    VALUES(?, ?, ?)
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, gameName);
                preparedStatement.setString(2, gameJson);
                preparedStatement.setString(3, spectatorJson);
                preparedStatement.executeUpdate();
                var rs = preparedStatement.getGeneratedKeys();
                if(rs.next()) {
                    return rs.getInt(1);
                }
            } catch(SQLException sqlEx) {}
        } catch(SQLException sqlEx) {}
        catch(DataAccessException e) {}
        return 0;
    }

    @Override
    public Collection<AbbreviatedGameData> listGames() {
        try(var conn = DatabaseManager.getConnection()) {
            ArrayList<AbbreviatedGameData> games = new ArrayList<>();
            var statement = """
                    SELECT * FROM GameData
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                try(var rs = preparedStatement.executeQuery()) {
                    while(rs.next()) {
                        int id = rs.getInt("id");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");
                        String gameName = rs.getString("gameName");
                        AbbreviatedGameData nextGame = new AbbreviatedGameData(id, whiteUsername, blackUsername, gameName);
                        games.add(nextGame);
                    }
                    return games;
                } catch(SQLException sqlEx) {}
            } catch(SQLException sqlEx) {}
        } catch(SQLException sqlEx) {}
        catch(DataAccessException e) {}
        return null;
    }
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS GameData(
                id INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                chessGame VARCHAR(255) NOT NULL,
                spectators VARCHAR(255) NOT NULL,
                PRIMARY KEY (id)
                );
            """
    };
    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
