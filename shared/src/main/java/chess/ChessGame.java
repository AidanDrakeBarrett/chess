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
        ChessPosition start = move.getStartPosition();
        if(board.getPiece(start) == null) {
            throw new InvalidMoveException("invalid move");
        }
        if(board.getPiece(start).getTeamColor() != teamTurn) {
            throw new InvalidMoveException("invalid move");
        }
        HashSet<ChessMove> validMoves = (HashSet<ChessMove>) validMoves(start);
        if(!validMoves.isEmpty()) {
            if(validMoves.contains(move)) {
                ChessPiece movePiece = board.getPiece(start);
                ChessPosition end = move.getEndPosition();
                if(move.getPromotionPiece() == null) {
                    board.addPiece(end, movePiece);
                    board.addPiece(start, null);
                }
                if(move.getPromotionPiece() != null) {
                    ChessPiece.PieceType promotion = move.getPromotionPiece();
                    ChessPiece promotedPiece = new ChessPiece(movePiece.getTeamColor(), promotion);
                    board.addPiece(end, promotedPiece);
                    board.addPiece(start, null);
                }
                if(movePiece.getTeamColor() == TeamColor.WHITE) {
                    setTeamTurn(TeamColor.BLACK);
                }
                if(movePiece.getTeamColor() == TeamColor.BLACK) {
                    setTeamTurn(TeamColor.WHITE);
                }
            }
            if(!validMoves.contains(move)) {
                throw new InvalidMoveException("invalid move");
            }
        }
        if(validMoves.isEmpty()) {
            throw new InvalidMoveException("invalid move");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        int kingRow = 0;
        int kingCol = 0;
        for(int i = 1; i <= 8; ++i) {
            boolean foundKing = false;
            for(int j = 1; j <= 8; ++j) {
                ChessPosition kingPos = new ChessPosition(i, j);
                if(board.getPiece(kingPos) != null) {
                    if(board.getPiece(kingPos).getPieceType() == ChessPiece.PieceType.KING) {
                        if(board.getPiece(kingPos).getTeamColor() == teamColor) {
                            kingRow = i;
                            kingCol = j;
                            foundKing = true;
                            break;
                        }
                    }
                }
            }
            if(foundKing) {
                break;
            }
        }
        ChessPosition kingPos = new ChessPosition(kingRow, kingCol);
        for(int i = 1; i <= 8; ++i) {
            for(int j = 1; j <= 8; ++j) {
                ChessPosition checkPos = new ChessPosition(i, j);
                if(board.getPiece(checkPos) != null) {
                    if(board.getPiece(checkPos).getTeamColor() != teamColor) {
                        HashSet<ChessMove> moves = (HashSet<ChessMove>) board.getPiece(checkPos).pieceMoves(board, checkPos);
                        for(ChessMove move:moves) {
                            if(move.getEndPosition().equals(kingPos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(isInCheck(teamColor)) {
            if(isInStalemate(teamColor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        for(int i = 1; i <= 8; ++i) {
            for(int j = 1; j <= 8; ++j) {
                ChessPosition stalePos = new ChessPosition(i, j);
                if(board.getPiece(stalePos) != null) {
                    if(board.getPiece(stalePos).getTeamColor() == teamColor) {
                        HashSet<ChessMove> goodMoves = (HashSet<ChessMove>) validMoves(stalePos);
                        if(!goodMoves.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
