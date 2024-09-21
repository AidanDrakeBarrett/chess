package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board = new ChessBoard();
    private TeamColor teamTurn;

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if(board.getPiece(startPosition) == null) {
            return null;
        }

        HashSet<ChessMove> possibleMoves
                = (HashSet<ChessMove>) board.getPiece(startPosition).pieceMoves(board, startPosition);
        HashSet<ChessMove> returnableMoves = new HashSet<>();
        for(ChessMove move:possibleMoves) {
            ChessBoard boardCopy = boardCloner();
            ChessPosition possibleEnd = move.getEndPosition();
            ChessPiece movingPiece = boardCopy.getPiece(startPosition);
            board.addPiece(possibleEnd, movingPiece);
            board.addPiece(startPosition, null);
            TeamColor checkingColor = board.getPiece(possibleEnd).getTeamColor();
            if(!isInCheck(checkingColor)) {
                returnableMoves.add(move);
            }
            board = boardCopy;
        }
        return returnableMoves;
    }
    private ChessBoard boardCloner() {
        ChessBoard copyBoard = new ChessBoard();
        for(int i = 1; i <= 8; ++i) {
            for(int j = 1; j <= 8; ++j) {
                ChessPosition copyPosition = new ChessPosition(i, j);
                if(board.getPiece(copyPosition) != null) {
                    TeamColor copyColor = board.getPiece(copyPosition).getTeamColor();
                    ChessPiece.PieceType copyType = board.getPiece(copyPosition).getPieceType();
                    ChessPiece copyPiece = new ChessPiece(copyColor, copyType);
                    copyBoard.addPiece(copyPosition, copyPiece);
                }
            }
        }
        return copyBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        throw new RuntimeException("Not implemented");
    }
}
