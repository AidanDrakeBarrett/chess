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
        switch(type) {
            case PAWN:
                return pawnMoves(board, myPosition);
            case ROOK:
                return rookMoves(board, myPosition);
            case KNIGHT:
                return knightMoves(board, myPosition);
            case BISHOP:
                return bishopMoves(board, myPosition);
            case QUEEN:
                return queenMoves(board, myPosition);
            case KING:
                return kingMoves(board, myPosition);
        }
        return null;
    }
    private HashSet<ChessMove> pawnMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        if(color == ChessGame.TeamColor.WHITE) {
            if(start.getRow() == 2) {
                ChessPosition hurdle = new ChessPosition(3, start.getColumn());
                if(board.getPiece(hurdle) == null) {
                    ChessPosition doubleJump = new ChessPosition(4, start.getColumn());
                    pawnMoveAdder(start, doubleJump, board, moves);
                }
            }
            if(start.getRow() < 7) {
                ChessPosition forward = new ChessPosition(start.getRow() + 1, start.getColumn());
                pawnMoveAdder(start, forward, board, moves);
            }
            if(start.getRow() == 7) {
                ChessPosition forward = new ChessPosition(start.getRow() + 1, start.getColumn());
                if(board.getPiece(forward) == null) {
                    pawnUpgrades(start, forward, moves);
                }
            }
            whiteDiags(start, board, moves);
        }
        if(color == ChessGame.TeamColor.BLACK) {
            if(start.getRow() == 7) {
                ChessPosition hurdle = new ChessPosition(6, start.getColumn());
                if(board.getPiece(hurdle) == null) {
                    ChessPosition doubleJump = new ChessPosition(5, start.getColumn());
                    pawnMoveAdder(start, doubleJump, board, moves);
                }
            }
            if(start.getRow() > 2) {
                ChessPosition forward = new ChessPosition(start.getRow() - 1, start.getColumn());
                pawnMoveAdder(start, forward, board, moves);
            }
            if(start.getRow() == 2) {
                ChessPosition forward = new ChessPosition(start.getRow() - 1, start.getColumn());
                if(board.getPiece(forward) == null) {
                    pawnUpgrades(start, forward, moves);
                }
            }
            blackDiags(start, board, moves);
        }
        return moves;
    }
    private void whiteDiags(ChessPosition start, ChessBoard board, HashSet<ChessMove> moves) {
        if(start.getColumn() > 1) {
            ChessPosition leftDiag = new ChessPosition(start.getRow() + 1, start.getColumn() - 1);
            pawnConfrontations(start, leftDiag, board, moves);
        }
        if(start.getColumn() < 8) {
            ChessPosition rightDiag = new ChessPosition(start.getRow() + 1, start.getColumn() + 1);
            pawnConfrontations(start, rightDiag, board, moves);
        }
    }
    private void blackDiags(ChessPosition start, ChessBoard board, HashSet<ChessMove> moves) {
        if(start.getColumn() > 1) {
            ChessPosition leftDiag = new ChessPosition(start.getRow() - 1, start.getColumn() - 1);
            pawnConfrontations(start, leftDiag, board, moves);
        }
        if(start.getColumn() < 8) {
            ChessPosition rightDiag = new ChessPosition(start.getRow() - 1, start.getColumn() + 1);
            pawnConfrontations(start, rightDiag, board, moves);
        }
    }
    private void pawnConfrontations(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {
        if(board.getPiece(end) != null) {
            if(board.getPiece(end).getTeamColor() != color) {
                if(end.getRow() == 8 || end.getRow() == 1) {
                    pawnUpgrades(start, end, moves);
                    return;
                }
                ChessMove kill = new ChessMove(start, end);
                moves.add(kill);
            }
        }
    }
    private void pawnMoveAdder(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {
        if(board.getPiece(end) == null) {
            ChessMove newMove = new ChessMove(start, end);
            moves.add(newMove);
        }
    }
    private void pawnUpgrades(ChessPosition start, ChessPosition end, HashSet<ChessMove> moves) {
        ChessMove upgrade1 = new ChessMove(start, end, PieceType.ROOK);
        ChessMove upgrade2 = new ChessMove(start, end, PieceType.KNIGHT);
        ChessMove upgrade3 = new ChessMove(start, end, PieceType.BISHOP);
        ChessMove upgrade4 = new ChessMove(start, end, PieceType.QUEEN);
        moves.add(upgrade1);
        moves.add(upgrade2);
        moves.add(upgrade3);
        moves.add(upgrade4);
    }
    private HashSet<ChessMove> rookMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        for(int i = 1; i + start.getRow() <= 8; ++i) {
            ChessPosition end = new ChessPosition(start.getRow() + i, start.getColumn());
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; start.getRow() - i >= 1; ++i) {
            ChessPosition end = new ChessPosition(start.getRow() - i, start.getColumn());
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; i + start.getColumn() <= 8; ++i) {
            ChessPosition end = new ChessPosition(start.getRow(), start.getColumn() + i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; start.getColumn() - i >= 1; ++i) {
            ChessPosition end = new ChessPosition(start.getRow(), start.getColumn() - i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        return moves;
    }
    private HashSet<ChessMove> knightMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        if(start.getRow() + 2 <= 8) {
            if(start.getColumn() - 1 >= 1) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() + 2, start.getColumn() - 1);
                moveAdder(start, wheelSpoke, board, moves);
            }
            if(start.getColumn() + 1 <= 8) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() + 2, start.getColumn() + 1);
                moveAdder(start, wheelSpoke, board, moves);
            }
        }
        if(start.getRow() - 2 >= 1) {
            if(start.getColumn() - 1 >= 1) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() - 2, start.getColumn() - 1);
                moveAdder(start, wheelSpoke, board, moves);
            }
            if(start.getColumn() + 1 <= 8) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() - 2, start.getColumn() + 1);
                moveAdder(start, wheelSpoke, board, moves);
            }
        }
        if(start.getRow() + 1 <= 8) {
            if(start.getColumn() - 2 >= 1) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() + 1, start.getColumn() - 2);
                moveAdder(start, wheelSpoke, board, moves);
            }
            if(start.getColumn() + 2 <= 8) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() + 1, start.getColumn() + 2);
                moveAdder(start, wheelSpoke, board, moves);
            }
        }
        if(start.getRow() - 1 >= 1) {
            if(start.getColumn() - 2 >= 1) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() - 1, start.getColumn() - 2);
                moveAdder(start, wheelSpoke, board, moves);
            }
            if(start.getColumn() + 2 <= 8) {
                ChessPosition wheelSpoke = new ChessPosition(start.getRow() - 1, start.getColumn() + 2);
                moveAdder(start, wheelSpoke, board, moves);
            }
        }
        return moves;
    }
    private HashSet<ChessMove> bishopMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        for(int i = 1; (start.getRow() + i <= 8) && (start.getColumn() + i <= 8); ++i) {
            ChessPosition end = new ChessPosition(start.getRow() + i, start.getColumn() + i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; (start.getRow() + i <= 8) && (start.getColumn() - i >= 1); ++i) {
            ChessPosition end = new ChessPosition(start.getRow() + i, start.getColumn() - i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; (start.getRow() - i >= 1) && (start.getColumn() + i <= 8); ++i) {
            ChessPosition end = new ChessPosition(start.getRow() - i, start.getColumn() + i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        for(int i = 1; (start.getRow() - i >= 1) && (start.getColumn() - i >= 1); ++i) {
            ChessPosition end = new ChessPosition(start.getRow() - i, start.getColumn() - i);
            boolean confrontation = moveAdder(start, end, board, moves);
            if(confrontation) {
                break;
            }
        }
        return moves;
    }
    private HashSet<ChessMove> queenMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        moves.addAll(rookMoves(board, start));
        moves.addAll(bishopMoves(board, start));
        return moves;
    }
    private HashSet<ChessMove> kingMoves(ChessBoard board, ChessPosition start) {
        HashSet<ChessMove> moves = new HashSet<>();
        if(start.getRow() + 1 <= 8) {
            if(start.getColumn() - 1 >= 1) {
                ChessPosition end = new ChessPosition(start.getRow() + 1, start.getColumn() - 1);
                moveAdder(start, end, board, moves);
            }
            if(start.getColumn() + 1 <= 8) {
                ChessPosition end = new ChessPosition(start.getRow() + 1, start.getColumn() + 1);
                moveAdder(start, end, board, moves);
            }
            ChessPosition end = new ChessPosition(start.getRow() + 1, start.getColumn());
            moveAdder(start, end, board, moves);
        }
        if(start.getRow() - 1 >= 1) {
            if(start.getColumn() - 1 >= 1) {
                ChessPosition end = new ChessPosition(start.getRow() - 1, start.getColumn() - 1);
                moveAdder(start, end, board, moves);
            }
            if(start.getColumn() + 1 <= 8) {
                ChessPosition end = new ChessPosition(start.getRow() - 1, start.getColumn() + 1);
                moveAdder(start, end, board, moves);
            }
            ChessPosition end = new ChessPosition(start.getRow() - 1, start.getColumn());
            moveAdder(start, end, board, moves);
        }
        if(start.getColumn() + 1 <= 8) {
            ChessPosition end = new ChessPosition(start.getRow(), start.getColumn() + 1);
            moveAdder(start, end, board, moves);
        }
        if(start.getColumn() - 1 >= 1) {
            ChessPosition end = new ChessPosition(start.getRow(), start.getColumn() - 1);
            moveAdder(start, end, board, moves);
        }
        return moves;
    }
    private boolean moveAdder(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {
        boolean confrontation = false;
        if(board.getPiece(end) == null) {
            ChessMove possibleMove = new ChessMove(start, end);
            moves.add(possibleMove);
        }
        if(board.getPiece(end) != null) {
            pieceConfrontations(start, end, board, moves);
            confrontation = true;
        }
        return confrontation;
    }
    private void pieceConfrontations(ChessPosition start, ChessPosition end, ChessBoard board, HashSet<ChessMove> moves) {
        if(board.getPiece(end).getTeamColor() != color) {
            ChessMove kill = new ChessMove(start, end);
            moves.add(kill);
        }
    }

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
