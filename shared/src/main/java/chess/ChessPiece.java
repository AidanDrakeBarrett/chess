package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        throw new RuntimeException("Not implemented");
    }
    private HashSet<ChessMove> pawnMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private void whiteDiags(ChessBoard board, ChessPosition start, HashSet<ChessMove> moves) {}
    private void blackDiags(ChessBoard board, ChessPosition start, HashSet<ChessMove> moves) {}
    private void pawnConfrontations(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {}
    private void pawnMoveAdder(ChessPosition start, ChessPosition end, HashSet<ChessMove> moves) {}
    private HashSet<ChessMove> rookMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private HashSet<ChessMove> knightMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private HashSet<ChessMove> bishopMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private HashSet<ChessMove> queenMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private HashSet<ChessMove> kingMoves(ChessBoard board, ChessPosition start) {
        return null;
    }
    private boolean moveAdder(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {
        return false;
    }
    private void pieceConfrontations(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}
