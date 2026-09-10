public class PawnPiece extends Piece {
    public PawnPiece(boolean isPlayer) { super("pawn", 40, 10, isPlayer); }
    
    @Override
    public boolean isValidMove(int targetX, int targetY) {
        int direction = isPlayer ? -1 : 1;
        int dx = Math.abs(targetX - getX());
        int dy = targetY - getY();
        return (dx == 0 && dy == direction) || (dx == 1 && dy == direction);
    }
}