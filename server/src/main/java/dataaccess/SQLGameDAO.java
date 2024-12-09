package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;

import com.google.gson.Gson;
import records.GameData;
import records.ResponseException;
import records.AbbreviatedGameData;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.Objects;

//import static dataaccess.DatabaseConfigurer.configureDatabase;

public class SQLGameDAO implements GameDAO{
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS GameData(
                id INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                chessGame VARCHAR(2047) NOT NULL,
                isActive BOOLEAN NOT NULL,
                PRIMARY KEY (id)
                );
            """
    };
    public SQLGameDAO() {
        try {
            DatabaseManager.configureDatabase(createStatements);
        } catch(ResponseException | DataAccessException e) {}
    }
    @Override
    public void clearData() {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    TRUNCATE TABLE GameData;
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
    }

    @Override
    public GameData getGame(int gameID) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                    SELECT * FROM GameData
                    WHERE id = ?;
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
                        boolean activity = rs.getBoolean("isActive");
                        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);
                        GameData returnGame = new GameData(id, whiteUsername, blackUsername,
                                gameName, chessGame, activity);
                        return returnGame;
                    }
                } catch(SQLException e) {
                    throw new DataAccessException("");
                }
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
        return null;
    }

    @Override
    public void joinGame(String username, ChessGame.TeamColor clientColor, int gameID) throws DataAccessException {
        try(var conn = DatabaseManager.getConnection()) {
            var idQuery = """
                    SELECT * FROM GameData
                    WHERE id = ?;
                    """;
            try(var preparedIDQuery = conn.prepareStatement(idQuery)) {
                boolean validID = false;
                preparedIDQuery.setInt(1, gameID);
                var rs = preparedIDQuery.executeQuery();
                while(rs.next()) {
                    int checkedID = rs.getInt("gameID");
                    if(checkedID == gameID) {
                        validID = true;
                        break;
                    }
                }
                if(!validID) {
                    throw new DataAccessException("Error: bad request");
                }
            } catch(SQLException e) {}
            if(clientColor == ChessGame.TeamColor.WHITE || clientColor == ChessGame.TeamColor.BLACK) {
                String statement = null;
                String columnLabel = null;
                if(clientColor == ChessGame.TeamColor.WHITE) {
                    columnLabel = "whiteUsername";
                    statement = """
                        SELECT whiteUsername FROM GameData
                        WHERE id = ?;
                        """;
                }
                if(clientColor == ChessGame.TeamColor.BLACK) {
                    columnLabel = "blackUsername";
                    statement = """
                        SELECT blackUsername FROM GameData
                        WHERE id = ?;
                        """;
                }
                try(var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.setInt(1, gameID);
                    var rs = preparedStatement.executeQuery();
                    String currentPlayer = null;
                    while(rs.next()) {
                        currentPlayer = rs.getString(columnLabel);
                    }
                    if(Objects.equals(currentPlayer, null)) {
                        playerInserter(username, columnLabel, gameID, conn);
                        return;
                    }
                    throw new DataAccessException("Error: already taken");
                } catch(SQLException e) {}
            }
            if(clientColor == null) {
                throw new DataAccessException("Error: bad request");
            }
        } catch(SQLException e) {}
    }
    private void playerInserter(String username, String colorColumn, int gameID, Connection conn) throws SQLException {
        var statement = """
                UPDATE GameData SET
                """
                + colorColumn +
                """
                 = ? WHERE id = ?;
                """;
        try(var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, gameID);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public int createGame(String gameName) {
        try(var conn = DatabaseManager.getConnection()) {
            ChessGame game = new ChessGame();
            var gameJson = new Gson().toJson(game);
            var statement = """
                    INSERT INTO GameData
                    (gameName, chessGame, isActive)
                    VALUES (?, ?, ?);
                    """;
            try(var preparedStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameName);
                preparedStatement.setString(2, gameJson);
                preparedStatement.setBoolean(3, true);
                preparedStatement.executeUpdate();
                var rs = preparedStatement.getGeneratedKeys();
                rs.next();
                int newGameID = rs.getInt(1);
                return newGameID;
            } catch(SQLException e) {}
        } catch(SQLException |DataAccessException e) {}
        return 0;
    }

    @Override
    public Collection<AbbreviatedGameData> listGames() {
        try(var conn = DatabaseManager.getConnection()) {
            ArrayList<AbbreviatedGameData> games = new ArrayList<>();
            var statement = """
                    SELECT * FROM GameData
                    WHERE isActive = 1;
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
                } catch(SQLException e) {}
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
        return null;
    }
    public void updateBoard(int gameID, ChessGame game) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                UPDATE GameData
                SET chessGame = ?
                WHERE id = ?;
                """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                var gameJson = new Gson().toJson(game);
                preparedStatement.setString(1, gameJson);
                preparedStatement.setInt(2, gameID);
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
    }
    public void endGame(int gameID) {
        try(var conn = DatabaseManager.getConnection()) {
            var statement = """
                UPDATE GameData
                SET isActive = ?
                WHERE id = ?;
                """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setBoolean(1, false);
                preparedStatement.setInt(2, gameID);
                preparedStatement.executeUpdate();
            } catch(SQLException e) {}
        } catch(SQLException | DataAccessException e) {}
    }
    public void removePlayer(int gameID, String colorColumn) {
        try(var conn = DatabaseManager.getConnection()) {
            playerInserter(null, colorColumn, gameID, conn);
        } catch(SQLException | DataAccessException e) {}
    }
}
