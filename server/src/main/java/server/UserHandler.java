package server;

import com.google.gson.Gson;
import data.access.AuthData;
import data.access.UserData;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class UserHandler {
    private static UserService service = new UserService();

    public UserHandler() {}
    public static Object register(Request req, Response res) {
        var newUser = new Gson().fromJson(req.body(), UserData.class);
        if((newUser.username() == null) || (newUser.password() == null) || (newUser.email() == null)) {
            String message = "Error: bad request";
            res.status(400);
            return new Gson().toJson(Map.of("message", message));
        }
        AuthData newAuth = null;
        try {
            newAuth = service.register(newUser);
        } catch(ResponseException resEx) {
            String message = "Error: already taken";
            res.status(403);
            return new Gson().toJson(Map.of("message", message));
        }
        return new Gson().toJson(newAuth);
    }
    public static Object login(Request req, Response res) {
        var userLogin = new Gson().fromJson(req.body(), UserData.class);
        AuthData newAuth = null;
        try {
            newAuth = service.login(userLogin);
        } catch(ResponseException resEx) {
            String message = "Error: unauthorized";
            res.status(401);
            return new Gson().toJson(Map.of("message", message));
        }
        return new Gson().toJson(newAuth);
    }
    public static Object logout(Request req, Response res) {
        var authToken = req.headers("Authorization");
        try {
            service.logout(authToken);
        } catch(ResponseException resEx) {
            String message = "Error: unauthorized";
            res.status(401);
            return new Gson().toJson(Map.of("message", message));
        }
        return "{}";
    }
}
