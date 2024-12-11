package ui;

import chess.*;
import records.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import java.util.*;
import static java.lang.Integer.parseInt;

public class Client implements ServerMessageHandler {
    private final ServerFacade serverFacade;
    private WebSocketFacade ws = null;
    private UserState state;
    private int gameListSize = 0;
    private ChessGame game;
    private ChessGame.TeamColor playerColor;
    private enum UserState {
        LOGGED_OUT,
        LOGGED_IN,
        IN_GAME,
        WATCHING,
        MAY_RESIGN
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
        if(state == UserState.IN_GAME) {
            helpCommands.append("move <START POSITION> <END POSITION> <PROMOTION>\n");
            helpCommands.append("\tmove a piece from one square to another, formatting positions like so: a1.\n");
            helpCommands.append("\tfor example: 'move b2 b3' would move the piece at b2 to b3.\n");
            helpCommands.append("\twrite the promotion using the new piece's letter: r, n, b, q\n");
            helpCommands.append("\talternatively, you can write the name of the piece: rook, knight, etc.\n");
            helpCommands.append("legal <POSITION>\n");
            helpCommands.append("\tenter a piece's position to see the places it can legally move be highlighted\n");
            helpCommands.append("redraw\n");
            helpCommands.append("\tredraws the board\n");
            helpCommands.append("resign\n");
            helpCommands.append("\tasks if you want to resign. Yes means you forfeit and end the game for everyone.\n");
            helpCommands.append("leave\n");
            helpCommands.append("\tremoves you from the game without ending it\n");
            helpCommands.append("help\n");
            helpCommands.append("\tdisplay possible commands\n");
        }
        if(state == UserState.WATCHING) {
            helpCommands.append("redraw\n");
            helpCommands.append("\tredraws the board\n");
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
            if(state == UserState.LOGGED_OUT) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> "quit";
                    case "create", "list", "join", "observe", "logout", "move", "legal", "redraw", "resign", "leave" ->
                            "Error: you must be logged in to use that command\n";
                    default -> help();
                };
            }
            if(state == UserState.LOGGED_IN) {
                return switch (cmd) {
                    case "create" -> create(params);
                    case "list" -> list();
                    case "join", "observe" -> join(params);
                    case "quit" -> "quit";
                    case "logout" -> logout();
                    case "register", "login" -> "Error: you can not use that command while already logged in\n";
                    case "move", "legal", "redraw", "resign", "leave" ->
                            "Error: you must join a game to use that command\n";
                    default -> help();
                };
            }
            if(state == UserState.WATCHING) {
                return switch (cmd) {
                    case "redraw" -> redraw();
                    case "leave" -> leave();
                    case "register", "login" -> "Error: you can not use that command while already logged in\n";
                    case "create", "list", "join", "observe", "quit", "logout" ->
                            "Error: please leave your current game first\n";
                    case "move", "legal", "resign" -> "Error: only players can use those commands\n";
                    default -> help();
                };
            }
            if(state == UserState.IN_GAME) {
                return switch (cmd) {
                    case "move" -> makeMove(params);
                    case "legal" -> legalMoves(params);
                    case "redraw" -> redraw();
                    case "resign" -> resignPrompt();
                    case "leave" -> leave();
                    case "register", "login" ->
                            "Error: you can not use that command while already logged in\n";
                    case "create", "list", "join", "observe", "quit", "logout" ->
                            "Error: please leave your current game first\n";
                    default -> help();
                };
            }
            if(state == UserState.MAY_RESIGN) {
                return switch (cmd) {
                    case "y", "yes" -> resign();
                    case "n", "no" -> backToGame();
                    default -> resignPrompt();
                };
            }
        } catch(RuntimeException e) {
            return e.getMessage();
        }
        return null;
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
    public String join(String... params) throws RuntimeException {
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
                state = UserState.IN_GAME;
                playerColor = ChessGame.TeamColor.WHITE;
            }
            if(color == ChessGame.TeamColor.BLACK) {
                state = UserState.IN_GAME;
                playerColor = ChessGame.TeamColor.BLACK;
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
        return "logged out\n";
    }
    public String drawBoard(ChessPiece[][] board, HashSet<ChessPosition> legalEnds, ChessPosition start) {
        StringBuilder view = new StringBuilder();
        if(playerColor == ChessGame.TeamColor.WHITE || state == UserState.WATCHING) {
            view.append(DrawBoard.drawWhite(board, legalEnds, start, view));
        }
        if(state == UserState.WATCHING) {
            view.append("\u001b[39;49;0m\n");
        }
        if(playerColor == ChessGame.TeamColor.BLACK || state == UserState.WATCHING) {
            view.append(DrawBoard.drawBlack(board, legalEnds, start, view));
        }
        return view.toString();
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
            System.out.print(redraw());
        }
        printPrompt();
    }
    public String makeMove(String... params) throws RuntimeException {
        if(params.length >= 2) {
            String start = params[0];
            String end = params[1];
            ChessPosition startPos = coordParser(start);
            if(game.getBoard().getPiece(startPos).getTeamColor() != playerColor) {
                throw new RuntimeException("Error: %s is not your piece.\n");
            }
            ChessPosition endPos = coordParser(end);
            ChessPiece.PieceType promotion = null;
            if(params.length == 3) {
                if(game.getBoard().getPiece(startPos).getPieceType() != ChessPiece.PieceType.PAWN) {
                    throw new RuntimeException("Error: this piece cannot be promoted\n");
                }
                if(playerColor == ChessGame.TeamColor.WHITE && endPos.getRow() != 8) {
                    throw new RuntimeException("Error: this move cannot result in a promotion\n");
                }
                if(playerColor == ChessGame.TeamColor.BLACK && endPos.getRow() != 1) {
                    throw new RuntimeException("Error: this move cannot result in a promotion\n");
                }
                promotion = pieceParser(params[2]);
            }
            ChessMove move = new ChessMove(startPos, endPos, promotion);
            try {
                ws.makeMove(move);
            } catch(Exception e) {
                throw new RuntimeException("Error: unknown\n");
            }
        }
        return null;
    }
    private ChessPiece.PieceType pieceParser(String piece) {
        return switch (piece) {
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            default -> throw new RuntimeException("Error: please provide a valid piece for promotion\n");
        };
    }
    private ChessPosition coordParser(String coord) throws RuntimeException {
        if(coord.length() == 2) {
            char file = coord.charAt(0);
            char rank = coord.charAt(1);
            int col = file - 96;
            if(col < 1 || col > 8) {
                throw new RuntimeException(String.format("Error: %s is not within 'a'-'h.'\n", file));
            }
            int row = parseInt(String.valueOf(rank));
            if(row < 1 || row > 8) {
                throw new RuntimeException(String.format("Error: %s is not within 1-8\n", rank));
            }
            return new ChessPosition(row, col);
        }
        throw new RuntimeException(String.format("Error: %s is not a valid position.\n", coord));
    }
    public String legalMoves(String... params) {
        if(params.length == 1) {
            ChessPosition start = coordParser(params[0]);
            HashSet<ChessMove> validMoves = (HashSet<ChessMove>) game.validMoves(start);
            HashSet<ChessPosition> ends = new HashSet<>();
            for(ChessMove move:validMoves) {
                ends.add(move.getEndPosition());
            }
            System.out.print(drawBoard(game.getBoard().getBoard(), ends, start));
            return null;
        }
        throw new RuntimeException("Error: bad input\n");
    }
    public String redraw() {
        return drawBoard(game.getBoard().getBoard(), null, null);
    }
    public String resignPrompt() {
        state = UserState.MAY_RESIGN;
        return "Are you sure you want to resign? This means forfeiting the game. <Y|YES|N|NO>\n";
    }
    public String resign() {
        try {
            ws.resign();
            state = UserState.LOGGED_IN;
            return null;
        } catch(Exception e) {
            throw new RuntimeException("Error: unknown\n");
        }
    }
    public String backToGame() {
        state = UserState.IN_GAME;
        return "You are still in the game\n";
    }
    public String leave() {
        try {
            ws.leave();
            state = UserState.LOGGED_IN;
            return null;
        } catch(Exception e) {
            throw new RuntimeException("Error: unknown");
        }
    }
}