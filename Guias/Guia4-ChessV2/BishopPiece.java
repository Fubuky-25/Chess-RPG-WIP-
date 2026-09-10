/** Alfil: diagonales, sin saltar piezas y limitado por su alcance. */
public class BishopPiece extends Piece
{
    public BishopPiece(boolean isPlayer) { super("bishop", 60, 16, 3, isPlayer); }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        if (!isDiagonalLine(dx, dy)) return false;
        return chebyshev(dx, dy) <= getMoveRange();
    }
}
