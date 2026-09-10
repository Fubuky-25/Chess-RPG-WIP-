public class KingPiece extends Piece {
    public KingPiece(boolean isPlayer) { super("king", 120, 15, isPlayer); }
    
    @Override
    public boolean isValidMove(int targetX, int targetY) {
        int dx = Math.abs(targetX - getX());
        int dy = Math.abs(targetY - getY());
        return (dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0);
    }
}