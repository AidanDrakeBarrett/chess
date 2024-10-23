package server;

import dataAccess.AbbreviatedGameData;
import service.GamesService;
import spark.Request;
import spark.Response;

import java.util.ArrayList;

public class GamesHandler {
    private static GamesService service = new GamesService();

    public GamesHandler() {}
    public static Object listGames(Request req, Response res) {
        var authToken = req.headers("Authorization");
        ArrayList<AbbreviatedGameData> games = null;
        try {
            games = (ArrayList<AbbreviatedGameData>) service.listGames(authToken);
        }
    }
}
