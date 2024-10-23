package server;

import service.ClearAppService;
import spark.Request;
import spark.Response;

public class ClearAppHandler {
    private static ClearAppService service = new ClearAppService();

    public ClearAppHandler() {}
    public static Object clearApplication(Request req, Response res) {
        service.clearApplication();
        res.status(200);
        return "{}";
    }
}
