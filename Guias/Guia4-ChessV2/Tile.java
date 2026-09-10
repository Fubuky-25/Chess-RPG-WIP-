import greenfoot.*;

/**
 * Casilla del tablero. Se mantiene siempre en su celda (inmovilidad en edicion)
 * y puede resaltarse para mostrar movimientos y ataques validos.
 */
public class Tile extends Actor
{
    public static final int HL_NONE = 0;
    public static final int HL_MOVE = 1;    // verde
    public static final int HL_ATTACK = 2;  // rojo
    public static final int HL_SELECTED = 3;// azul
    public static final int HL_DEPLOY = 4;  // celeste tenue

    private int fixedX;
    private int fixedY;
    private String type;
    private int highlight = HL_NONE;

    public Tile(String type, int x, int y)
    {
        this.type = type;
        this.fixedX = x;
        this.fixedY = y;
        redraw();
    }

    public void act()
    {
        // Bloquea el arrastre manteniendo las casillas estaticas
        if (getX() != fixedX || getY() != fixedY) {
            setLocation(fixedX, fixedY);
        }
    }

    public void setHighlight(int h)
    {
        if (h != highlight) {
            highlight = h;
            redraw();
        }
    }

    public int getHighlight() { return highlight; }

    private void redraw()
    {
        GreenfootImage img = new GreenfootImage(imageFileFor(type));
        if (img.getWidth() != ChessWorld.CELL_SIZE || img.getHeight() != ChessWorld.CELL_SIZE) {
            img.scale(ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE);
        }
        if (highlight == HL_MOVE) {
            img.setColor(new Color(40, 220, 60, 110));
            img.fillRect(0, 0, ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE);
        } else if (highlight == HL_ATTACK) {
            img.setColor(new Color(230, 40, 40, 130));
            img.fillRect(0, 0, ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE);
        } else if (highlight == HL_SELECTED) {
            img.setColor(new Color(40, 130, 255, 120));
            img.fillRect(0, 0, ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE);
        } else if (highlight == HL_DEPLOY) {
            img.setColor(new Color(255, 220, 60, 70));
            img.fillRect(0, 0, ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE);
        }
        setImage(img);
    }

    private String imageFileFor(String t)
    {
        if (t.equalsIgnoreCase("White"))  return "White.png";
        if (t.equalsIgnoreCase("Black"))  return "Black.png";
        return "background.png";
    }

    public int getBoardX() { return fixedX; }
    public int getBoardY() { return fixedY; }
}
