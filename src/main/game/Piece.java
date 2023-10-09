package game;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class Piece implements chess.ChessPiece {
    private ChessGame.TeamColor teamColor;
    private PieceType pieceType;

    //private ChessMove lastMove = null;

    public Piece(ChessGame.TeamColor color, PieceType piece) {
        teamColor = color;
        pieceType = piece;
    }

    /*public Piece(ChessGame.TeamColor color, PieceType piece, ChessMove lastMove) {
        teamColor = color;
        pieceType = piece;
        this.lastMove = lastMove;
    }*/
    @Override
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    @Override
    public PieceType getPieceType() {
        return pieceType;
    }

    /*public ChessMove getLastMove() {
        return lastMove;
    }*/

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int column = myPosition.getColumn();
        Position endPosition;
        switch (pieceType) {
            case KING -> {
                if (column < 8) {
                    endPosition = new Position(row, column + 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 1 && column > 1) {
                    endPosition = new Position(row - 1, column - 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 1) {
                    endPosition = new Position(row - 1, column);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 1 && column < 8) {
                    endPosition = new Position(row - 1, column + 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (column > 1) {
                    endPosition = new Position(row, column - 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 8 && column > 1) {
                    endPosition = new Position(row + 1, column - 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 8) {
                    endPosition = new Position(row + 1, column);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 8 && column < 8) {
                    endPosition = new Position(row + 1, column + 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                /*if (lastMove == null) {
                    Piece leftCorner = (Piece) board.getPiece(new Position(myPosition.getRow(), 1));
                    if (leftCorner != null && leftCorner.getTeamColor() == teamColor
                            && leftCorner.getPieceType() == PieceType.ROOK
                            && leftCorner.getLastMove() == null
                            && board.getPiece(new Position(myPosition.getRow(), 2)) == null
                            && board.getPiece(new Position(myPosition.getRow(), 3)) == null
                            && board.getPiece(new Position(myPosition.getRow(), 4)) == null) {
                    }
                }*/
                //FIXME add for castling
            }
            case PAWN -> {
                if (teamColor == ChessGame.TeamColor.WHITE) {
                    endPosition = new Position(row + 1, column);
                    if (board.getPiece(endPosition) == null) {
                        if (row == 7) {
                            moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                            moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                            moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                            moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                        }
                        else moves.add(new Move(myPosition, endPosition));
                    }
                    if (row == 2) {
                        endPosition = new Position(row + 2, column);
                        if (board.getPiece(endPosition) == null) moves.add(new Move(myPosition, endPosition));
                    }
                    if (column > 1) {
                        endPosition = new Position(row + 1, column - 1);
                        if (board.getPiece(endPosition) != null
                                && board.getPiece(endPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                            if (row == 7) {
                                moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                                moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                                moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                                moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                            }
                            else moves.add(new Move(myPosition, endPosition));
                        }
                    }
                    if (column < 8) {
                        endPosition = new Position(row + 1, column + 1);
                        if (board.getPiece(endPosition) != null
                                && board.getPiece(endPosition).getTeamColor() == ChessGame.TeamColor.BLACK) {
                            if (row == 7) {
                                moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                                moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                                moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                                moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                            }
                            else moves.add(new Move(myPosition, endPosition));
                        }
                    }
                    //FIXME add en passant
                }
                else if (teamColor == ChessGame.TeamColor.BLACK) {
                    endPosition = new Position(row - 1, column);
                    if (board.getPiece(endPosition) == null) {
                        if (row == 2) {
                            moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                            moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                            moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                            moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                        }
                        else moves.add(new Move(myPosition, endPosition));
                    }
                    if (row == 7) {
                        endPosition = new Position(row - 2, column);
                        if (board.getPiece(endPosition) == null) moves.add(new Move(myPosition, endPosition));
                    }
                    if (column > 1) {
                        endPosition = new Position(row - 1, column - 1);
                        if (board.getPiece(endPosition) != null
                                && board.getPiece(endPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                            if (row == 2) {
                                moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                                moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                                moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                                moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                            }
                            else moves.add(new Move(myPosition, endPosition));
                        }
                    }
                    if (column < 8) {
                        endPosition = new Position(row - 1, column + 1);
                        if (board.getPiece(endPosition) != null
                                && board.getPiece(endPosition).getTeamColor() == ChessGame.TeamColor.WHITE) {
                            if (row == 2) {
                                moves.add(new Move(myPosition, endPosition, PieceType.QUEEN));
                                moves.add(new Move(myPosition, endPosition, PieceType.BISHOP));
                                moves.add(new Move(myPosition, endPosition, PieceType.KNIGHT));
                                moves.add(new Move(myPosition, endPosition, PieceType.ROOK));
                            }
                            else moves.add(new Move(myPosition, endPosition));
                        }
                    }
                    //FIXME add en passant
                }
            }
            case ROOK -> {
                int newRow = row - 1;
                while (newRow > 0 && board.getPiece(new Position(newRow, column)) == null) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                }
                if (newRow > 0 && board.getPiece(new Position(newRow, column)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                while (newRow < 9 && board.getPiece(new Position(newRow, column)) == null) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                }
                if (newRow < 9 && board.getPiece(new Position(newRow, column)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                }

                int newCol = column - 1;
                while (newCol > 0 && board.getPiece(new Position(row, newCol)) == null) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newCol;
                }
                if (newCol > 0 && board.getPiece(new Position(row, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newCol = column + 1;
                while (newCol < 9 && board.getPiece(new Position(row, newCol)) == null) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newCol;
                }
                if (newCol < 9 && board.getPiece(new Position(row, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }
            }
            case KNIGHT -> {
                if (row > 2 && column > 1) {
                    endPosition = new Position(row - 2, column - 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 1 && column > 2) {
                    endPosition = new Position(row - 1, column - 2);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 1 && column < 7) {
                    endPosition = new Position(row - 1, column + 2);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row > 2 && column < 8) {
                    endPosition = new Position(row - 2, column + 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 7 && column > 1) {
                    endPosition = new Position(row + 2, column - 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 8 && column > 2) {
                    endPosition = new Position(row + 1, column - 2);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 8 && column < 7) {
                    endPosition = new Position(row + 1, column + 2);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
                if (row < 7 && column < 8) {
                    endPosition = new Position(row + 2, column + 1);
                    if (board.getPiece(endPosition) == null
                            || board.getPiece(endPosition).getTeamColor() != teamColor) {
                        moves.add(new Move(myPosition, endPosition));
                    }
                }
            }
            case BISHOP -> {
                int newRow = row - 1;
                int newCol = column - 1;
                while (newRow > 0 && newCol > 0 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                    --newCol;
                }
                if (newRow > 0 && newCol > 0
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row - 1;
                newCol = column + 1;
                while (newRow > 0 && newCol < 9 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                    ++newCol;
                }
                if (newRow > 0 && newCol < 9
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                newCol = column - 1;
                while (newRow < 9 && newCol > 0 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                    --newCol;
                }
                if (newRow < 9 && newCol > 0
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                newCol = column + 1;
                while (newRow < 9 && newCol < 9 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                    ++newCol;
                }
                if (newRow < 9 && newCol < 9
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }
            }
            case QUEEN -> {
                int newRow = row - 1;
                while (newRow > 0 && board.getPiece(new Position(newRow, column)) == null) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                }
                if (newRow > 0 && board.getPiece(new Position(newRow, column)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                while (newRow < 9 && board.getPiece(new Position(newRow, column)) == null) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                }
                if (newRow < 9 && board.getPiece(new Position(newRow, column)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, column);
                    moves.add(new Move(myPosition, endPosition));
                }

                int newCol = column - 1;
                while (newCol > 0 && board.getPiece(new Position(row, newCol)) == null) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newCol;
                }
                if (newCol > 0 && board.getPiece(new Position(row, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newCol = column + 1;
                while (newCol < 9 && board.getPiece(new Position(row, newCol)) == null) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newCol;
                }
                if (newCol < 9 && board.getPiece(new Position(row, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(row, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row - 1;
                newCol = column - 1;
                while (newRow > 0 && newCol > 0 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                    --newCol;
                }
                if (newRow > 0 && newCol > 0
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row - 1;
                newCol = column + 1;
                while (newRow > 0 && newCol < 9 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    --newRow;
                    ++newCol;
                }
                if (newRow > 0 && newCol < 9
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                newCol = column - 1;
                while (newRow < 9 && newCol > 0 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                    --newCol;
                }
                if (newRow < 9 && newCol > 0
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }

                newRow = row + 1;
                newCol = column + 1;
                while (newRow < 9 && newCol < 9 && board.getPiece(new Position(newRow, newCol)) == null) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                    ++newRow;
                    ++newCol;
                }
                if (newRow < 9 && newCol < 9
                        && board.getPiece(new Position(newRow, newCol)).getTeamColor() != teamColor) {
                    endPosition = new Position(newRow, newCol);
                    moves.add(new Move(myPosition, endPosition));
                }
            }
        }
        return moves;
    }
}
