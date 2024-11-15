import chess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        Server server = new Server();
        try {
            var port = 8080;
            if(args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
            server.run(port);
        } catch(Throwable e) {
            System.out.printf("Unable to start server: %s%n", e.getMessage());
        }
    }
}