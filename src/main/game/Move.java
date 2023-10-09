package game;

import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Objects;

public class Move implements chess.ChessMove {
    private ChessPosition start;

    private ChessPosition end;

    private ChessPiece.PieceType type = null;

    public Move(ChessPosition start, ChessPosition end) {
        this.start = start;
        this.end = end;
    }

    public Move(ChessPosition start, ChessPosition end, ChessPiece.PieceType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }
    @Override
    public ChessPosition getStartPosition() {
        return start;
    }

    @Override
    public ChessPosition getEndPosition() {
        return end;
    }

    @Override
    public ChessPiece.PieceType getPromotionPiece() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        if (!((Move)obj).start.equals(this.start)) return false;
        if (!((Move)obj).end.equals(this.end)) return false;
        if (!(((Move)obj).type == this.type)) return false;
        return true;
        /*return (((Move)obj).start.equals(this.start) && ((Move)obj).end.equals(this.end)
                && ((Move)obj).type.equals(this.type));*/
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, type);
    }
}
