package game;

import java.util.Objects;

public class Position implements chess.ChessPosition {
    private int row;
    private int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;
    }
    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        return (((Position) obj).row == this.row && ((Position)obj).column == this.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}
