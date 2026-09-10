public class RookPiece extends Piece {
    public RookPiece(boolean isPlayer) { super("rook", 90, 18, isPlayer); }
    
    @Override
    public boolean isValidMove(int targetX, int targetY) {
        return (getX() == targetX || getY() == targetY);
    }
}