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
    public void run() {}
    private void printPrompt() {
        System.out.print("\n\u001b[15;40;0m>>>");
    }
    public void notify(Notification notification) {}
    public String help() {}
    public String eval(String input) {}
    public String register(String... params) throws ResponseException {}
    public login(String... params) throws ResponseException {}
    public String create(String... params) throws ResponseException {}
    public String list() throws ResponseException {}
    public String join() throws ResponseException {}
    public String logout() throws ResponseException {}
    public String drawBoard(ChessPiece[][] board) {}

}
