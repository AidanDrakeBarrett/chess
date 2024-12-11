package ui;

import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class DrawBoard {
    public static StringBuilder drawWhite(ChessPiece[][] board, HashSet<ChessPosition> legalEnds, ChessPosition start,
                                          StringBuilder view) {
        for (int i = 8; i >= 0; --i) {
            for (int j = 0; j < 9; ++j) {
                if(start != null) {
                    ChessPosition highlight = new ChessPosition(i, j);
                    if(legalEnds.contains(highlight)) {
                        drawWhiteView(i, j, true, false, view, board);
                    } else {
                        drawWhiteView(i, j, false, highlight.equals(start), view, board);
                    }
                } else {
                    drawWhiteView(i, j, false, false, view, board);
                }
            }
        }
        return view;
    }
    private static void drawWhiteView(int row, int col, boolean legalEnd, boolean startPos, StringBuilder whiteView,
                                      ChessPiece[][] board) {
        if(row == 0 && col == 0) {
            whiteView.append("\u001b[30;107;1m    a  b  c  d  e  f  g  h \u001b[39;49;0m\n");
        }
        if(row > 0) {
            if(col == 0) {
                whiteView.append(String.format("\u001b[30;107;1m %d ", row));
            }
            if(col > 0) {
                String background = backgroundColor(row, col, legalEnd, startPos);
                if(board[row - 1][col - 1] != null) {
                    ChessPiece piece = board[row - 1][col - 1];
                    String pieceColor = pieceColorer(piece);
                    String pieceType = pieceTyper(piece);
                    whiteView.append(String.format(pieceColor + background + pieceType));
                }
                if(board[row - 1][col - 1] == null) {
                    whiteView.append(String.format("\u001b[" + background + "   "));
                }
                if(col == 8) {
                    whiteView.append("\u001b[39;49;0m\n");
                }
            }
        }
    }
    public static StringBuilder drawBlack(ChessPiece[][] board, HashSet<ChessPosition> legalEnds, ChessPosition start, StringBuilder view) {
        for (int i = 0; i < 9; ++i) {
            for (int j = 8; j >= 0; --j) {
                if (start != null) {
                    ChessPosition highlight = new ChessPosition(i, j);
                    if (legalEnds.contains(highlight)) {
                        drawBlackView(i, j, true, false, view, board);
                    } else {
                        drawBlackView(i, j, false, highlight.equals(start), view, board);
                    }
                } else {
                    drawBlackView(i, j, false, false, view, board);
                }
            }
        }
        return view;
    }
    private static void drawBlackView(int row, int col, boolean legalEnd, boolean startPos, StringBuilder blackView,
                                      ChessPiece[][] board) {
        if(row == 8 && col == 8) {
            blackView.append("\u001b[30;107;1m    h  g  f  e  d  c  b  a \u001b[39;49;0m\n");
        }
        if(row < 8) {
            if(col == 8) {
                blackView.append(String.format("\u001b[30;107;1m %d ", (row + 1)));
            }
            if(col < 8) {
                String background = backgroundColor(row, col, legalEnd, startPos);
                if(board[row][col] != null) {
                    ChessPiece piece = board[row][col];
                    String pieceColor = pieceColorer(piece);
                    String pieceType = pieceTyper(piece);
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
    private static String backgroundColor(int row, int col, boolean legalEnd, boolean startPos) {
        if(startPos) {
            return "103;1m";
        }
        if(legalEnd) {
            return "106;1m";
        }
        if((row % 2) == (col % 2)) {
            return "102;1m";
        }
        if((row % 2) != (col % 2)) {
            return "107;1m";
        }
        return null;
    }
    private static String pieceTyper(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case PAWN -> " P ";
            case ROOK -> " R ";
            case KNIGHT -> " N ";
            case BISHOP -> " B ";
            case KING -> " K ";
            case QUEEN -> " Q ";
        };
    }
    private static String pieceColorer(ChessPiece piece) {
        return switch (piece.getTeamColor()) {
            case WHITE -> "\u001b[37;";
            case BLACK -> "\u001b[30;";
        };
    }

}
