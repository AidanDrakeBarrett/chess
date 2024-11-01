package server;

import com.google.gson.Gson;
import dataaccess.AbbreviatedGameData;
import service.GamesService;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Map;

public class GamesHandler {
    private static GamesService service = new GamesService();

    public GamesHandler() {}
    public static Object listGames(Request req, Response res) {
        var authToken = req.headers("Authorization");
        ArrayList<AbbreviatedGameData> games = null;
        try {
            games = (ArrayList<AbbreviatedGameData>) service.listGames(authToken);
        } catch(ResponseException resEx) {
            String message = "Error: unauthorized";
            res.status(401);
            return new Gson().toJson(Map.of("message", message));
        }
        return new Gson().toJson(Map.of("games", games));
    }
    public static Object createGame(Request req, Response res) {
        var authToken = req.headers("Authorization");
        var gameRequest = new Gson().fromJson(req.body(), Map.class);
        String gameName = (String) gameRequest.get("gameName");
        int gameID;
        try {
            gameID = (int) service.createGame(authToken, gameName);
        } catch(ResponseException resEx) {
            String message = "Error: unauthorized";
            res.status(401);
            return new Gson().toJson(Map.of("message", message));
        }
        return new Gson().toJson(Map.of("gameID", gameID));
    }
    public static Object joinGame(Request req, Response res) {
        var authToken = req.headers("Authorization");
        var joinReq = new Gson().fromJson(req.body(), JoinRequests.class);
        var playerColor = joinReq.playerColor();
        int gameID = joinReq.gameID();
        try {
            service.joinGame(authToken, playerColor, gameID);
        } catch(ResponseException resEx) {
            res.status(resEx.getStatusCode());
            return new Gson().toJson(Map.of("message", resEx.getMessage()));
        }
        res.status(200);
        return "{}";
    }
}
