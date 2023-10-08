package game;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class Piece implements chess.ChessPiece {
    private ChessGame.TeamColor teamColor;
    private PieceType pieceType;

    public Piece(ChessGame.TeamColor color, PieceType piece) {
        this.teamColor = color;
        this.pieceType = piece;
    }
    @Override
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    public PieceType getPieceType() {
        return pieceType;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return null;
    }
}
