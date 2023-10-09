package game;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class Game implements chess.ChessGame {
    private TeamColor turn = TeamColor.WHITE;
    private ChessBoard board = new Board();
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
            ChessPiece temp = board.getPiece(move.getEndPosition());
            ChessPiece oldPiece = piece;
            if (move.getPromotionPiece() != null) {
                piece = new Piece(oldPiece.getTeamColor(), move.getPromotionPiece()/*, move*/);
            }
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (!isInCheck(piece.getTeamColor())) {
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
        Collection<ChessMove> validMoves = piece.pieceMoves(board, move.getStartPosition());
        boolean containsMove = false;
        for (ChessMove currMove : validMoves) {
            if (currMove.equals(move)) containsMove = true;
        }
        if (!containsMove) throw new InvalidMoveException("Move is against the rules.");

        ChessPiece temp = board.getPiece(move.getEndPosition());
        ChessPiece oldPiece = piece;
        if (move.getPromotionPiece() != null) {
            piece = new Piece(oldPiece.getTeamColor(), move.getPromotionPiece()/*, move*/);
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        if (isInCheck(turn)) {
            board.addPiece(move.getStartPosition(), oldPiece);
            board.addPiece(move.getEndPosition(), temp);
            throw new InvalidMoveException("Move puts your king in check.");
        }
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
}
