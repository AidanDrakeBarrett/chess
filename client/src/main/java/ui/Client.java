package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import records.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.Integer.parseInt;


public class Client implements ServerMessageHandler {
    private final ServerFacade serverFacade;
    private WebSocketFacade ws = null;
    private UserState state;
    private int gameListSize = 0;
    private ChessGame game;

    private enum UserState {
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME_WHITE,
        IN_GAME_BLACK,
        WATCHING
    }
    public Client(String url) {
        serverFacade = new ServerFacade(url);
        state = UserState.LOGGED_OUT;
    }
    public void run() {
        System.out.println("Welcome to my chess server. I don't know why you're still using cURL in 2024, but okay.\n");
        System.out.println("Try typing 'help' for commands\n");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
    private void printPrompt() {
        System.out.print("\n\u001b[15;40;0m>>>");
    }
    public String help() {
        StringBuilder helpCommands = new StringBuilder();
        if(state == UserState.LOGGED_OUT) {
            helpCommands.append("register <USERNAME> <PASSWORD> <EMAIL>\n");
            helpCommands.append("\tcreate an account\n");
            helpCommands.append("login <USERNAME> <PASSWORD>\n");
            helpCommands.append("\tlogin to a preexisting account\n");
            helpCommands.append("quit\n");
            helpCommands.append("\tleave the serverFacade\n");
            helpCommands.append("help\n");
            helpCommands.append("\tdisplay possible commands\n");
        }
        if(state == UserState.LOGGED_IN) {
            helpCommands.append("create <NAME>\n");
            helpCommands.append("\tstart a new game\n");
            helpCommands.append("list\n");
            helpCommands.append("\tlist games that have already been created\n");
            helpCommands.append("join <GAME ID> <WHITE|BLACK|EMPTY>\n");
            helpCommands.append("\tjoin a game using its ID and which color you want to play" +
                    ", or leave blank to spectate\n");
            helpCommands.append("observe <GAME ID>\n");
            helpCommands.append("\tanother way to spectate a game\n");
            helpCommands.append("quit\n");
            helpCommands.append("\tleave the server\n");
            helpCommands.append("logout\n");
            helpCommands.append("\tlog out of your account\n");
            helpCommands.append("help\n");
            helpCommands.append("\tdisplay possible commands\n");
        }
        if(state == UserState.IN_GAME_WHITE || state == UserState.IN_GAME_BLACK) {
            helpCommands.append("make_move <START POSITION> <END POSITION>\n");
            helpCommands.append("\tmove a piece from one square to another, formatting positions as so: a1.\n");
            helpCommands.append("\tfor example: 'make_move b2 b3' would move the piece as b2 to b3.\n");
            helpCommands.append("legal_moves <POSITION>\n");
            helpCommands.append("\tenter a piece's position to see the places it can legally move be highlighted\n");
            helpCommands.append("redraw\n");
            helpCommands.append("\tshows the most recent board. Useful for seeing the game after someone moves\n");
            helpCommands.append("resign\n");
            helpCommands.append("\tasks if you want to resign. Yes means you forfeit and end the game for everyone.\n");
            helpCommands.append("leave\n");
            helpCommands.append("\tremoves you from the game without ending it\n");
            helpCommands.append("help\n");
            helpCommands.append("\tdisplay possible commands\n");
        }
        if(state == UserState.WATCHING) {
            helpCommands.append("redraw\n");
            helpCommands.append("\tshows the most recent board. Useful for seeing the game after someone moves\n");
            helpCommands.append("leave\n");
            helpCommands.append("\tremoves you from the game without ending it\n");
            helpCommands.append("help\n");
            helpCommands.append("\tdisplay possible commands\n");
        }
        return helpCommands.toString();
    }
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> join(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                case "make_move" -> makeMove(params);
                case "legal_moves" -> legalMoves(params);
                case "redraw" -> redraw();
                case "resign" -> resign();
                case "leave" -> leave();
                default -> help();
            };
        } catch(RuntimeException e) {
            return e.getMessage();
        }
    }
    public String register(String... params) throws RuntimeException {
        if(params.length >= 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            UserData newUser = new UserData(username, password, email);
            serverFacade.register(newUser);
            state = UserState.LOGGED_IN;
            return String.format("Registered and signed in as %s.", username);
        }
        throw new RuntimeException("Error: make sure username, password, and email " +
                "are filled out with a space between each field");
    }
    public String login(String... params) throws RuntimeException {
        if(params.length >= 2) {
            String username = params[0];
            String password = params[1];
            UserData newLogin = new UserData(username, password, null);
            serverFacade.login(newLogin);
            state = UserState.LOGGED_IN;
            return String.format("Logged in as %s", username);
        }
        throw new RuntimeException("Error: make sure both username and password are written with a space between them");
    }
    public String create(String... params) throws RuntimeException {
        if(params.length >= 1) {
            String gameName = params[0];
            serverFacade.create(gameName);
            return String.format("Created game %s.", gameName);
        }
        throw new RuntimeException("Error: please provide a name for your new game.");
    }
    public String list() throws RuntimeException {
        ArrayList<AbbreviatedGameData> gameList = serverFacade.list();
        StringBuilder returnListBuilder = new StringBuilder("Current chess games:\n");
        for(AbbreviatedGameData game:gameList) {
            returnListBuilder.append(String.format("game#: %d, whiteUsername: %s, blackUsername: %s, gameName: %s\n"
                    , game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        gameListSize = gameList.size();
        return returnListBuilder.toString();
    }
    public String join(String... params) throws RuntimeException {//FIXME: APPARENTLY ONLY THE PLAYERS NEED THIS CODE, BUT IS IT WRONG FOR ME TO STILL FUNNEL EVERYONE THROUGH HERE? MAYBE NOT.
        if(params.length >= 1) {
            int gameNumber;
            try {
                gameNumber = parseInt(params[0]);
            } catch(NumberFormatException e) {
                throw new RuntimeException(String.format("Error: %s is not a valid game number.", params[0]));
            }
            if(gameNumber < 0 || gameNumber > gameListSize) {
                throw new RuntimeException(String.format("Error: %s is not a valid game number.", params[0]));
            }
            ChessGame.TeamColor color = null;
            String position = "spectator";
            if(params.length >= 2) {
                if(!Objects.equals(params[1], "white") && !Objects.equals(params[1], "black")) {
                    throw new RuntimeException("Error: player color must be 'white', 'black', or left empty to watch.");
                }
                if(Objects.equals(params[1], "white")) {
                    color = ChessGame.TeamColor.WHITE;
                    position = "white";
                }
                if(Objects.equals(params[1], "black")) {
                    color = ChessGame.TeamColor.BLACK;
                    position = "black";
                }
            }
            this.ws = serverFacade.join(gameNumber, color);
            try {
                ws.joinGame(UserGameCommand.CommandType.CONNECT);
            } catch(Exception e) {
                throw new RuntimeException("Error: failed to connect.");
            }
            if(color == ChessGame.TeamColor.WHITE) {
                state = UserState.IN_GAME_WHITE;
            }
            if(color == ChessGame.TeamColor.BLACK) {
                state = UserState.IN_GAME_BLACK;
            }
            if(color == null) {
                state = UserState.WATCHING;
            }
            return String.format("joined game as " + position + "\n");
        }
        throw new RuntimeException("Error: please provide a game number. " +
                "If you are joining a game to play, provide a valid color too.");
    }
    public String logout() throws RuntimeException {
        serverFacade.logout();
        state = UserState.LOGGED_OUT;
        return String.format("logged out");
    }
    public String drawBoard(ChessPiece[][] board, ChessGame.TeamColor color) {
        StringBuilder view = new StringBuilder();
        if(color == ChessGame.TeamColor.WHITE) {
            for (int i = 8; i >= 0; --i) {
                for (int j = 0; j < 9; ++j) {
                    drawWhiteView(i, j, view, board);
                }
            }
        }
        if(color == ChessGame.TeamColor.BLACK) {
            for (int i = 0; i < 9; ++i) {
                for (int j = 8; j >= 0; --j) {
                    drawBlackView(i, j, view, board);
                }
            }
        }
        return view.toString();
    }
    private void drawWhiteView(int row, int col, StringBuilder whiteView, ChessPiece[][] board) {
        if(row == 0 && col == 0) {
            whiteView.append("\u001b[30;107;1m    a  b  c  d  e  f  g  h \u001b[39;49;0m\n");
        }
        if(row > 0) {
            if (col == 0) {
                whiteView.append(String.format("\u001b[30;107;1m %d ", row));
            }
            if (col > 0) {
                String background = null;
                if ((row % 2) == (col % 2)) {
                    background = "102;1m";
                }
                if ((row % 2) != (col % 2)) {
                    background = "47;1m";
                }
                if (board[row - 1][col - 1] != null) {
                    ChessPiece piece = board[row - 1][col - 1];
                    String pieceColor = null;
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        pieceColor = "\u001b[15;";
                    }
                    if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        pieceColor = "\u001b[30;";
                    }
                    String pieceType = null;
                    switch (piece.getPieceType()) {
                        case PAWN -> pieceType = " P ";
                        case ROOK -> pieceType = " R ";
                        case KNIGHT -> pieceType = " N ";
                        case BISHOP -> pieceType = " B ";
                        case KING -> pieceType = " K ";
                        case QUEEN -> pieceType = " Q ";
                    }
                    whiteView.append(String.format(pieceColor + background + pieceType));
                }
                if (board[row - 1][col - 1] == null) {
                    whiteView.append(String.format("\u001b[" + background + "   "));
                }
                if (col == 8) {
                    whiteView.append("\u001b[39;49;0m\n");
                }
            }
        }
    }
    private void drawBlackView(int row, int col, StringBuilder blackView, ChessPiece[][] board) {
        if(row == 8 && col == 8) {
            blackView.append("\u001b[30;107;1m    h  g  f  e  d  c  b  a \u001b[39;49;0m\n");
        }
        if(row < 8) {
            if(col == 8) {
                blackView.append(String.format("\u001b[30;107;1m %d ", (row + 1)));
            }
            if(col < 8) {
                String background = null;
                if((row % 2) == (col % 2)) {
                    background = "102;1m";
                }
                if((row % 2) != (col % 2)) {
                    background = "47;1m";
                }
                if(board[row][col] != null) {
                    ChessPiece piece = board[row][col];
                    String pieceColor = null;
                    if(piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        pieceColor = "\u001b[15;";
                    }
                    if(piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        pieceColor = "\u001b[30;";
                    }
                    String pieceType = null;
                    switch (piece.getPieceType()) {
                        case PAWN -> pieceType = " P ";
                        case ROOK -> pieceType = " R ";
                        case KNIGHT -> pieceType = " N ";
                        case BISHOP -> pieceType = " B ";
                        case KING -> pieceType = " K ";
                        case QUEEN -> pieceType = " Q ";
                    }
                    blackView.append(String.format(pieceColor + background + pieceType));
                }
                if(board[row][col] == null) {
                    blackView.append(String.format("\u001b[" + background + "   "));
                }
                if(col == 0) {
                    blackView.append("\u001b[39;49;0m\n");
                }
            }
        }
    }
    @Override
    public void notify(ServerMessage message) {
        if(message.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            System.out.println(message.getMessage());
        }
        if(message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            System.out.println(message.getMessage());
        }
        if(message.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            this.game = message.getGame().chessGame();
            if(state == UserState.IN_GAME_WHITE) {
                System.out.print(drawBoard(game.getBoard().getBoard(), ChessGame.TeamColor.WHITE));
            }
            if(state == UserState.IN_GAME_BLACK) {
                System.out.print(drawBoard(game.getBoard().getBoard(), ChessGame.TeamColor.BLACK));
            }
        }
        printPrompt();
    }
    public String makeMove(String... params) {//FIXME: EVERY FUNCTION FROM THIS LINE ONWARDS NEEDS TO BE IMPLEMENTED. LIKELY, THE WEB SOCKET CLASSES WILL BE FORCED INTO IMPLEMENTATION.
        return null;
    }//TODO: IMPLEMENT
    public String legalMoves(String... params) {
        return null;
    }
    public String redraw() {
        return null;
    }
    public String resign() {
        return null;
    }
    public String leave() {
        return null;
    }
}
