import greenfoot.*;

public class PieceThemeManager {
    public static boolean isPlayerWhite = true; // Se define al inicio de cada nivel

    public static void assignRandomColor() {
        isPlayerWhite = Greenfoot.getRandomNumber(2) == 0;
    }

    public static void applyImage(Piece piece, String pieceType, boolean isPlayer) {
        // Determina si esta pieza en concreto debe ser blanca o negra
        boolean useWhite = isPlayer ? isPlayerWhite : !isPlayerWhite;
        String colorPrefix = useWhite ? "white_" : "black_";
        
        // Carga la imagen (ej: "white_pawn.png", "black_king.png")
        piece.setImage(colorPrefix + pieceType.toLowerCase() + ".png");
    }
}