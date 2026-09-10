import greenfoot.*;
import java.util.List;

/**
 * Gestor de apariencia: decide de que color juega el jugador en cada nivel
 * y entrega el sprite correcto a cada pieza.
 */
public class PieceThemeManager
{
    /** true = el jugador usa las piezas blancas en este nivel. */
    public static boolean isPlayerWhite = true;

    /** Se llama al inicio de cada nivel: re-sortea el bando visual. */
    public static void assignRandomColor()
    {
        isPlayerWhite = Greenfoot.getRandomNumber(2) == 0;
    }

    /** Devuelve el sprite correspondiente (ej: "white_king.png"). */
    public static GreenfootImage getSprite(String pieceType, boolean isPlayer)
    {
        boolean useWhite = isPlayer ? isPlayerWhite : !isPlayerWhite;
        String colorPrefix = useWhite ? "white_" : "black_";
        GreenfootImage img = new GreenfootImage(colorPrefix + pieceType.toLowerCase() + ".png");
        int target = ChessWorld.CELL_SIZE - 8;
        if (img.getWidth() != target || img.getHeight() != target) {
            img.scale(target, target);
        }
        return img;
    }

    /** Compatibilidad: aplica el sprite directamente sobre la pieza. */
    public static void applyImage(Piece piece, String pieceType, boolean isPlayer)
    {
        piece.refreshImage();
    }

    /**
     * Re-tinta TODAS las piezas del mundo. Necesario porque el color se
     * re-sortea cada nivel y las piezas supervivientes no se recrean.
     */
    public static void refreshAll(World world)
    {
        List<Piece> pieces = world.getObjects(Piece.class);
        for (Piece p : pieces) {
            p.invalidateSprite();   // el color del bando cambio: hay que recargar el PNG
            p.refreshImage();
        }
    }
}
