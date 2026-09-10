public class BishopPiece extends Piece {
    public BishopPiece(boolean isPlayer) { super("bishop", 60, 16, isPlayer); }
    
    @Override
    public boolean isValidMove(int targetX, int targetY) {
        return Math.abs(targetX - getX()) == Math.abs(targetY - getY());
    }
}