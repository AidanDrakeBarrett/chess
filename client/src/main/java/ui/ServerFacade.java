package ui;

import chess.ChessGame;
import records.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {
    private String authToken = null;
    private HashMap<Integer, AbbreviatedGameData> safeGameIndex = new HashMap<>();
    private final String serverURL;

    public ServerFacade(String url) {
        serverURL = url;
    }
    public void register(UserData newUser) throws ResponseException {
        var path = "/user";
        var body = new Gson().toJson(newUser);
        String method = "POST";
        this.authToken = sendRequest(path, method, body, authToken, AuthData.class).authToken();
    }
    public void login(UserData newLogin) throws ResponseException {
        var path = "/session";
        var body = new Gson().toJson(newLogin);
        String method = "POST";
        this.authToken = sendRequest(path, method, body, authToken, AuthData.class).authToken();
    }
    public void create(String gameName) throws ResponseException {
        String path = "/game";
        var body = new Gson().toJson(Map.of("gameName", gameName));
        String method = "POST";
        sendRequest(path, method, body, authToken, Map.class);
    }
    public ArrayList list() throws ResponseException {
        String path = "/game";
        String body = null;
        String method = "GET";
        var listMap = sendRequest(path, method, body, authToken, Map.class);
        var arrayJson = new Gson().toJson(listMap.get("games"));
        ArrayList<AbbreviatedGameData> rawGameArray = new Gson().fromJson(arrayJson,
                new TypeToken<ArrayList<AbbreviatedGameData>>(){}.getType());
        safeGameIndex.clear();
        ArrayList<AbbreviatedGameData> safeGameArray= new ArrayList<>();
        for(int i = 0; i < rawGameArray.size(); ++i) {
            String white = rawGameArray.get(i).whiteUsername();
            String black = rawGameArray.get(i).blackUsername();
            String name = rawGameArray.get(i).gameName();
            AbbreviatedGameData safeGame = new AbbreviatedGameData((i + 1), white, black, name);
            safeGameArray.add(safeGame);
            safeGameIndex.put((i + 1), rawGameArray.get(i));
        }
        return safeGameArray;
    }
    public void join(int safeGameNumber, ChessGame.TeamColor color) throws ResponseException {
        if(color != null) {
            String path = "/game";
            int gameID = safeGameIndex.get(safeGameNumber).gameID();
            var body = new Gson().toJson(new JoinRequests(color, gameID));
            String method = "PUT";
            sendRequest(path, method, body, authToken, null);
        }
    }
    public void logout() throws ResponseException {
        String path = "/session";
        String body = null;
        String method = "DELETE";
        sendRequest(path, method, body, authToken, null);
        authToken = null;
    }
    private <T> T sendRequest(String path, String method, String body, String authToken, Class<T> responseClass) throws
            ResponseException {
        try {
            URI uri = new URI(serverURL + path);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            if(authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }
            http.setRequestMethod(method);
            http.setDoOutput(true);
            writeRequestBody(body, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readResponseBody(http, responseClass);
        } catch(Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
    private static void writeRequestBody(String body, HttpURLConnection http) throws IOException {
        if(body != null) {
            http.addRequestProperty("Content-Type", "application/json");
            try(OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(body.getBytes());
            }
        }
    }
    private static <T> T readResponseBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if(http.getContentLength() < 0) {
            try(InputStream resBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(resBody);
                if(responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if(!successful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }
    private boolean successful(int status) {
        return status == 200;
    }
    public String getAuth() {
        return authToken;
    }
}
