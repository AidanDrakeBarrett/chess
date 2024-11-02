package dataaccess;

import chess.ChessGame;

import com.google.gson.Gson;
import server.ResponseException;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.HashSet;
import java.util.Objects;

import static dataaccess.DatabaseConfigurer.configureDatabase;

public class SQLGameDAO implements GameDAO{
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS GameData(
                id INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                chessGame VARCHAR(2047) NOT NULL,
                PRIMARY KEY (id)
                );
            """
    };
    public SQLGameDAO() {
        try {
            DatabaseConfigurer.configureDatabase(createStatements);
        } catch(ResponseException e) {}
        catch(DataAccessException e) {}
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
        } catch(SQLException e) {}
        catch(DataAccessException e) {}
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
                        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);
                        GameData returnGame = new GameData(id, whiteUsername, blackUsername, gameName, chessGame);
                        return returnGame;
                    }
                } catch(SQLException e) {}
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch(DataAccessException e) {}
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
                    (gameName, chessGame)
                    VALUES (?, ?);
                    """;
            try(var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.setString(1, gameName);
                preparedStatement.setString(2, gameJson);
                preparedStatement.executeUpdate();
                return newGameIDQuery(gameName, conn);
            } catch(SQLException e) {}
        } catch(SQLException e) {}
        catch(DataAccessException e) {}
        return 0;
    }
    private int newGameIDQuery(String gameName, Connection conn) throws SQLException {
        String statement = """
                SELECT id FROM GameData
                WHERE gameName = ?;
                """;
        try(var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setString(1, gameName);
            var rs = preparedStatement.executeQuery();
            int newID = 0;
            while(rs.next()) {
                if(newID < rs.getInt("id")) {
                    newID = rs.getInt("id");
                }
            }
            return newID;
        }
    }

    @Override
    public Collection<AbbreviatedGameData> listGames() {
        try(var conn = DatabaseManager.getConnection()) {
            ArrayList<AbbreviatedGameData> games = new ArrayList<>();
            var statement = """
                    SELECT * FROM GameData;
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
}
