/** Caballo: salto en L. Es la unica pieza que ignora el bloqueo de camino. */
public class KnightPiece extends Piece
{
    public KnightPiece(boolean isPlayer) { super("knight", 70, 20, 1, isPlayer); }

    @Override
    public boolean isValidMove(int targetX, int targetY)
    {
        int dx = Math.abs(targetX - getX());
        int dy = Math.abs(targetY - getY());
        return (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
    }

    @Override
    public boolean jumpsOverPieces() { return true; }
}
