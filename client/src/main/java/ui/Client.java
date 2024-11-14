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
    //And now for exposing endpoints, but I'm frankly tired, and ready for bed, so Imma just write a commit
}
