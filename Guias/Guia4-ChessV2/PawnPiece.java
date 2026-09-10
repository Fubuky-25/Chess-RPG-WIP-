/**
 * Peon: el caso que demuestra que MOVER y ATACAR no son lo mismo.
 * Avanza recto una casilla, pero captura en diagonal.
 */
public class PawnPiece extends Piece
{
    public PawnPiece(boolean isPlayer) { super("pawn", 40, 10, 1, isPlayer); }

    /** El jugador avanza hacia arriba (-1); los enemigos hacia abajo (+1). */
    private int forward() { return isPlayer ? -1 : 1; }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        return dx == 0 && dy == forward();
    }

    @Override
    public boolean isValidAttack(int targetX, int targetY)
    {
        int dx = targetX - getX();
        int dy = targetY - getY();
        return Math.abs(dx) == 1 && dy == forward();
    }
}
