/** Rey: una casilla en cualquier direccion. Si muere, GAME OVER. */
public class KingPiece extends Piece
{
    public KingPiece(boolean isPlayer) { super("king", 120, 15, 1, isPlayer); }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        // El Rey SIEMPRE se mueve 1 casilla, aunque una reliquia suba el alcance
        return chebyshev(dx, dy) == 1;
    }
}
