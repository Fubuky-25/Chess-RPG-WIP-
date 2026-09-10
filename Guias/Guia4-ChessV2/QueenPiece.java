/** Dama: rectas y diagonales, limitada por su alcance. */
public class QueenPiece extends Piece
{
    public QueenPiece(boolean isPlayer) { super("queen", 80, 22, 3, isPlayer); }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        if (dx == 0 && dy == 0) return false;
        if (!isStraightLine(dx, dy) && !isDiagonalLine(dx, dy)) return false;
        return chebyshev(dx, dy) <= getMoveRange();
    }
}
