/** Torre: lineas rectas, sin saltar piezas y limitada por su alcance. */
public class RookPiece extends Piece
{
    public RookPiece(boolean isPlayer) { super("rook", 90, 18, 3, isPlayer); }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        if (!isStraightLine(dx, dy)) return false;
        return chebyshev(dx, dy) <= getMoveRange();
    }
}
