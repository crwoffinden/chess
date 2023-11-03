package game;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class Game implements chess.ChessGame {
    private TeamColor turn = TeamColor.WHITE;
    private ChessBoard board = new Board();

    private ChessMove lastMove = null;
    @Override
    public TeamColor getTeamTurn() {
        return turn;
    }

    @Override
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    @Override
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return null;
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();
        for (ChessMove move : moves) {
            boolean valid = true;
            ChessPiece temp = board.getPiece(move.getEndPosition());
            ChessPiece oldPiece = piece;
            if (move.getPromotionPiece() != null) {
                piece = new Piece(oldPiece.getTeamColor(), move.getPromotionPiece(), move);
            }
            if (oldPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (move.getEndPosition().getColumn() != move.getStartPosition().getColumn()
                        && board.getPiece(move.getEndPosition()) == null) {
                    Piece capturedPawn = (Piece) board.getPiece(new Position(move.getStartPosition().getRow(),
                            move.getEndPosition().getColumn()));
                    if (lastMove.equals(capturedPawn.getLastMove())) {
                        board.addPiece(new Position(move.getStartPosition().getRow(), move.getEndPosition().getColumn()),
                                null);
                        if (isInCheck(turn)) valid = false;
                        board.addPiece(new Position(move.getStartPosition().getRow(), move.getEndPosition().getColumn()),
                                capturedPawn);
                    } else valid = false;
                }
            }
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (oldPiece.getPieceType() == ChessPiece.PieceType.KING) {
                if (move.getEndPosition().getColumn() == move.getStartPosition().getColumn() - 2) {
                    ChessPosition rookPosition = new Position(move.getStartPosition().getRow(),
                            move.getStartPosition().getColumn() - 1);
                    ChessPiece rook = board.getPiece(new Position(move.getStartPosition().getRow(), 1));
                    board.addPiece(rookPosition, rook);
                    board.addPiece(new Position(move.getStartPosition().getRow(), 1), null);
                    if (isInCheck(turn)) valid = false;
                    boolean rookCanBeCaptured = false;
                    for (int row = 1; row < 9; ++row) {
                        for (int column = 1; column < 9; ++column) {
                            Position currPosition = new Position(row, column);
                            ChessPiece oppPiece = board.getPiece(currPosition);
                            if (oppPiece != null && oppPiece.getTeamColor() != rook.getTeamColor()) {
                                Collection<ChessMove> pieceMoves = oppPiece.pieceMoves(board, currPosition);
                                for (ChessMove currMove : pieceMoves) {
                                    if (currMove.getEndPosition().equals(rookPosition)) rookCanBeCaptured = true;
                                }
                            }
                        }
                    }
                    if (rookCanBeCaptured) valid = false;
                    board.addPiece(new Position(move.getStartPosition().getRow(), 1), rook);
                    board.addPiece(rookPosition, null);
                } else if (move.getEndPosition().getColumn() == move.getStartPosition().getColumn() + 2) {
                    ChessPosition rookPosition = new Position(move.getStartPosition().getRow(),
                            move.getStartPosition().getColumn() + 1);
                    ChessPiece rook = board.getPiece(new Position(move.getStartPosition().getRow(), 8));
                    board.addPiece(rookPosition, rook);
                    board.addPiece(new Position(move.getStartPosition().getRow(), 8), null);
                    if (isInCheck(turn)) valid = false;
                    boolean rookCanBeCaptured = false;
                    for (int row = 1; row < 9; ++row) {
                        for (int column = 1; column < 9; ++column) {
                            Position currPosition = new Position(row, column);
                            ChessPiece oppPiece = board.getPiece(currPosition);
                            if (oppPiece != null && oppPiece.getTeamColor() != rook.getTeamColor()) {
                                Collection<ChessMove> pieceMoves = oppPiece.pieceMoves(board, currPosition);
                                for (ChessMove currMove : pieceMoves) {
                                    if (currMove.getEndPosition().equals(rookPosition)) rookCanBeCaptured = true;
                                }
                            }
                        }
                    }
                    if (rookCanBeCaptured) valid = false;
                    board.addPiece(new Position(move.getStartPosition().getRow(), 8), rook);
                    board.addPiece(rookPosition, null);
                }
            }

            if (valid && !isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            board.addPiece(move.getStartPosition(), oldPiece);
            board.addPiece(move.getEndPosition(), temp);
        }
        return validMoves;
    }

    @Override
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) throw new InvalidMoveException("Starting position does not contain a piece.");
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("Starting position contains piece of wrong color.");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) throw new InvalidMoveException("Move is against the rules.");
        else {
            piece = new Piece(piece.getTeamColor(), piece.getPieceType(), move);
            if (move.getPromotionPiece() != null) {
                piece = new Piece(piece.getTeamColor(), move.getPromotionPiece(), move);
            }
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (move.getEndPosition().getColumn() != move.getStartPosition().getColumn()
                        && board.getPiece(move.getEndPosition()) == null) {
                    board.addPiece(new Position(move.getStartPosition().getRow(), move.getEndPosition().getColumn()),
                            null);
                }
            }
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                if (move.getEndPosition().getColumn() == move.getStartPosition().getColumn() - 2) {
                    ChessPiece rook = board.getPiece(new Position(move.getStartPosition().getRow(), 1));
                    board.addPiece(new Position(move.getStartPosition().getRow(),
                            move.getStartPosition().getColumn() - 1), rook);
                    board.addPiece(new Position(move.getStartPosition().getRow(), 1), null);
                } else if (move.getEndPosition().getColumn() == move.getStartPosition().getColumn() + 2) {
                    ChessPiece rook = board.getPiece(new Position(move.getStartPosition().getRow(), 8));
                    board.addPiece(new Position(move.getStartPosition().getRow(),
                            move.getStartPosition().getColumn() + 1), rook);
                    board.addPiece(new Position(move.getStartPosition().getRow(), 8), null);
                }
            }
        }
        lastMove = move;
        if (turn == TeamColor.WHITE) turn = TeamColor.BLACK;
        else if (turn == TeamColor.BLACK) turn = TeamColor.WHITE;
    }

    @Override
    public boolean isInCheck(TeamColor teamColor) {
        Position kingPosition = null;
        int row = 1;
        int column = 1;
        while (kingPosition == null && row < 9) {
            while (kingPosition == null && column < 9) {
                ChessPiece currPiece = board.getPiece(new Position(row, column));
                if (currPiece != null && currPiece.getTeamColor() == teamColor
                        && currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPosition = new Position(row, column);
                }
                ++column;
            }
            ++row;
            column = 1;
        }
        if (kingPosition == null) return false;

        boolean canBeCaptured = false;
        for (row = 1; row < 9; ++row) {
            for (column = 1; column < 9; ++column) {
                Position currPosition = new Position(row, column);
                ChessPiece oppPiece = board.getPiece(currPosition);
                if (oppPiece != null && oppPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> pieceMoves = oppPiece.pieceMoves(board, currPosition);
                    for (ChessMove move : pieceMoves) {
                        if (move.getEndPosition().equals(kingPosition)) canBeCaptured = true;
                     }
                }
            }
        }
        return canBeCaptured;
    }

    @Override
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;
        boolean canBeStopped = false;
        for (int row = 1; row < 9; ++row) {
            for (int column = 1; column < 9; ++column) {
                ChessPiece currPiece = board.getPiece(new Position(row, column));
                if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = currPiece.pieceMoves(board, new Position(row, column));
                    for (ChessMove move : moves) {
                        ChessPiece temp = board.getPiece(move.getEndPosition());
                        board.addPiece(move.getEndPosition(), currPiece);
                        board.addPiece(move.getStartPosition(), null);
                        if (!isInCheck(teamColor)) canBeStopped = true;
                        board.addPiece(move.getStartPosition(), currPiece);
                        board.addPiece(move.getEndPosition(), temp);
                    }
                }
            }
        }
        return !canBeStopped;
    }

    @Override
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;
        boolean canMove = false;
        for (int row = 1; row < 9; ++row) {
            for (int column = 1; column < 9; ++column) {
                ChessPiece currPiece = board.getPiece(new Position(row, column));
                if (currPiece != null && currPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = currPiece.pieceMoves(board, new Position(row, column));
                    for (ChessMove move : moves) {
                        board.addPiece(move.getEndPosition(), currPiece);
                        board.addPiece(move.getStartPosition(), null);
                        if (!isInCheck(teamColor)) canMove = true;
                        board.addPiece(move.getStartPosition(), currPiece);
                        board.addPiece(move.getEndPosition(), null);
                    }
                }
            }
        }
        return !canMove;
    }

    @Override
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    @Override
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        return ((((Game)obj).turn == this.turn) && (((Game)obj).board.equals(this.board))
                && (((Game)obj).lastMove == this.lastMove));
    }
}
