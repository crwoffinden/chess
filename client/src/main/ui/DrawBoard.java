package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import game.Position;

import java.io.PrintStream;
import java.util.Set;

import static ui.EscapeSequences.*;

public class DrawBoard {
    private static final int BOARD_SIZE_IN_SQUARES = 8;

    public static void draw(PrintStream out, ChessGame game, ChessGame.TeamColor color, Set<ChessPosition> highlightSquares) {
        if (color == null) {
            draw(out, game, ChessGame.TeamColor.WHITE, null);
            out.print("\n");
            draw(out, game, ChessGame.TeamColor.BLACK, null);
        } else {
            //keeps track of the color of the squares
            // (since the bottom left corner is always black, the top left corner is always white)
            boolean white = true;
            //prints the column letters
            out.print(SET_BG_COLOR_DARK_GREEN);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print("   ");
            //orientation depends on the point of view
            if (color == ChessGame.TeamColor.WHITE) {
                for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                    char colName = (char) ('a' + col - 1);
                    out.print(" " + colName + " ");
                }
            } else {
                for (int col = 8; col > 0; --col) {
                    char colName = (char) ('a' + col - 1);
                    out.print(" " + colName + " ");
                }
            }
            out.print("   ");
            out.print("\n");
            //Orientation depends on the point of view
            if (color == ChessGame.TeamColor.WHITE) {
                for (int row = 8; row > 0; --row) {
                    out.print(SET_BG_COLOR_DARK_GREEN);
                    //Prints row number
                    out.print(" " + row + " ");
                    for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                        //prints the correct colored square
                        ChessPosition square = new Position(row, col);
                        if (highlightSquares != null && highlightSquares.contains(square))
                            out.print(SET_BG_COLOR_YELLOW);
                        else if (white) out.print(SET_BG_COLOR_LIGHT_GREY);
                        else out.print(SET_BG_COLOR_DARK_GREY);
                        //Finds the piece on that spot and prints accordingly
                        ChessPiece piece = game.getBoard().getPiece(square);
                        if (piece == null) out.print("   ");
                            //Uses letters because for some reason, unicode black pawns are wider than the other unicode pieces
                            //White pieces
                        else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            out.print(SET_TEXT_COLOR_WHITE);
                            switch (piece.getPieceType()) {
                                case PAWN:
                                    out.print(" P ");
                                    break;
                                case ROOK:
                                    out.print(" R ");
                                    break;
                                case KNIGHT:
                                    out.print(" N ");
                                    break;
                                case BISHOP:
                                    out.print(" B ");
                                    break;
                                case QUEEN:
                                    out.print(" Q ");
                                    break;
                                case KING:
                                    out.print(" K ");
                                    break;
                                default:
                                    out.print("   ");
                            }
                        }
                        //Black Pieces
                        else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                            out.print(SET_TEXT_COLOR_BLACK);
                            switch (piece.getPieceType()) {
                                case PAWN:
                                    out.print(" P ");
                                    break;
                                case ROOK:
                                    out.print(" R ");
                                    break;
                                case KNIGHT:
                                    out.print(" N ");
                                    break;
                                case BISHOP:
                                    out.print(" B ");
                                    break;
                                case QUEEN:
                                    out.print(" Q ");
                                    break;
                                case KING:
                                    out.print(" K ");
                                    break;
                                default:
                                    out.print("   ");
                            }
                        }
                        //Alternates the color of the next square
                        white = (!white);
                    }
                    out.print(SET_BG_COLOR_DARK_GREEN);
                    out.print(SET_TEXT_COLOR_BLACK);
                    //Prints the row number
                    out.print(" " + row + " ");
                    //Alternates the color of the first square on the next row
                    white = (!white);
                    out.print("\n");
                }
            }
            if (color == ChessGame.TeamColor.BLACK) {
                for (int row = 1; row <= BOARD_SIZE_IN_SQUARES; ++row) {
                    out.print(SET_BG_COLOR_DARK_GREEN);
                    //Prints the row number
                    out.print(" " + row + " ");
                    for (int col = 8; col > 0; --col) {
                        //Ensures the right square color
                        ChessPosition square = new Position(row, col);
                        if (highlightSquares != null && highlightSquares.contains(square))
                            out.print(SET_BG_COLOR_YELLOW);
                        else if (white) out.print(SET_BG_COLOR_LIGHT_GREY);
                        else out.print(SET_BG_COLOR_DARK_GREY);
                        //Finds the piece and prints accordingly
                        ChessPiece piece = game.getBoard().getPiece(square);
                        if (piece == null) out.print("   ");
                            //White pieces
                        else if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            out.print(SET_TEXT_COLOR_WHITE);
                            switch (piece.getPieceType()) {
                                case PAWN:
                                    out.print(" P ");
                                    break;
                                case ROOK:
                                    out.print(" R ");
                                    break;
                                case KNIGHT:
                                    out.print(" N ");
                                    break;
                                case BISHOP:
                                    out.print(" B ");
                                    break;
                                case QUEEN:
                                    out.print(" Q ");
                                    break;
                                case KING:
                                    out.print(" K ");
                                    break;
                                default:
                                    out.print("   ");
                            }
                        }
                        //Black pieces
                        else if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                            out.print(SET_TEXT_COLOR_BLACK);
                            switch (piece.getPieceType()) {
                                case PAWN:
                                    out.print(" P ");
                                    break;
                                case ROOK:
                                    out.print(" R ");
                                    break;
                                case KNIGHT:
                                    out.print(" N ");
                                    break;
                                case BISHOP:
                                    out.print(" B ");
                                    break;
                                case QUEEN:
                                    out.print(" Q ");
                                    break;
                                case KING:
                                    out.print(" K ");
                                    break;
                                default:
                                    out.print("  ");
                            }
                        }
                        //Alternates the color of the next square
                        white = (!white);
                    }
                    out.print(SET_BG_COLOR_DARK_GREEN);
                    out.print(SET_TEXT_COLOR_BLACK);
                    //Prints the row number
                    out.print(" " + row + " ");
                    //Alternates the color of the first square of the next row
                    white = (!white);
                    out.print("\n");
                }
            }
            out.print(SET_BG_COLOR_DARK_GREEN);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print("   ");
            //Prints the column letters
            //Orientation depends on point of view
            if (color == ChessGame.TeamColor.WHITE) {
                for (int col = 1; col <= BOARD_SIZE_IN_SQUARES; ++col) {
                    char colName = (char) ('a' + col - 1);
                    out.print(" " + colName + " ");
                }
            } else {
                for (int col = 8; col > 0; --col) {
                    char colName = (char) ('a' + col - 1);
                    out.print(" " + colName + " ");
                }
            }
            out.print("   ");
            out.print("\n");
            out.print(SET_BG_COLOR_BLACK);
            out.print(SET_TEXT_COLOR_WHITE);
        }
    }
}
