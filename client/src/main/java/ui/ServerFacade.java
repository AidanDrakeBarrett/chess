package ui;

import chess.ChessGame;
import chess.ChessPiece;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataaccess.AbbreviatedGameData;
import dataaccess.AuthData;
import dataaccess.UserData;
import server.JoinRequests;
import server.ResponseException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

public class ServerFacade {
    private String authToken = null;
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
