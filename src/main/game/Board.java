package game;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class Board implements chess.ChessBoard {
    private ChessPiece board[][] = new ChessPiece[8][8];

    @Override
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece; //FIXME check if rows need to be flipped
    }

    @Override
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }

    @Override
    public void resetBoard() {
        board[0][0] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        board[0][1] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][2] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][3] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        board[0][4] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        board[0][5] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][6] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][7] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        for (int i = 0; i < board[1].length; ++i) {
            board[1][i] = new Piece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        }
        for (int i = 2; i < 6; ++i) {
            for (int j = 0; j < board[i].length; ++j) {
                board[i][j] = null;
            }
        }
        for (int i = 0; i < board[6].length; ++i) {
            board[6][i] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        board[7][0] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        board[7][1] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][2] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][3] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        board[7][4] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        board[7][5] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][6] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][7] = new Piece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
    }
}
