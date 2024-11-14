package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import dataaccess.AbbreviatedGameData;
import dataaccess.UserData;
import server.ResponseException;

import javax.management.Notification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static java.awt.Color.RED;


public class Client {
    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    public Client(String url) {
        serverFacade = new ServerFacade(url);
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
    public void notify(Notification notification) {}
    public String help() {}
    public String eval(String input) {}
    public String register(String... params) throws ResponseException {}
    public String login(String... params) throws ResponseException {}
    public String create(String... params) throws ResponseException {}
    public String list() throws ResponseException {}
    public String join() throws ResponseException {}
    public String logout() throws ResponseException {}
    public String drawBoard(ChessPiece[][] board) {}

}
