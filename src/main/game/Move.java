package game;

import chess.ChessPiece;
import chess.ChessPosition;

public class Move implements chess.ChessMove {
    @Override
    public ChessPosition getStartPosition() {
        return null;
    }

    @Override
    public ChessPosition getEndPosition() {
        return null;
    }

    @Override
    public ChessPiece.PieceType getPromotionPiece() {
        return null;
    }
}
