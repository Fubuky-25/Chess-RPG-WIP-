import greenfoot.*;
import java.util.List;

/**
 * Icono arrastrable del panel de inventario.
 * Se suelta encima de una pieza del jugador para equiparla (solo en DEPLOY).
 */
public class ItemIcon extends Actor
{
    private Item item;
    private int homeX;
    private int homeY;
    private boolean dragging = false;

    public ItemIcon(Item item)
    {
        this.item = item;
        setImage(item.createIcon());
    }

    public void setHome(int x, int y) { homeX = x; homeY = y; }

    public void act()
    {
        World w = getWorld();
        if (w == null) return;
        if (!(w instanceof ChessWorld)) return;
        ChessWorld world = (ChessWorld) w;

        if (world.getState() != GameState.DEPLOY) {
            if (getX() != homeX || getY() != homeY) setLocation(homeX, homeY);
            return;
        }

        if (Greenfoot.mousePressed(this)) {
            dragging = true;
            world.showItemInfo(item);
        }

        if (dragging && Greenfoot.mouseDragged(this)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null) setLocation(mouse.getX(), mouse.getY());
        }

        if (dragging && Greenfoot.mouseDragEnded(this)) {
            dragging = false;
            MouseInfo mouse = Greenfoot.getMouseInfo();
            boolean equipped = false;
            if (mouse != null) {
                List<Piece> at = world.getObjectsAt(mouse.getX(), mouse.getY(), Piece.class);
                if (!at.isEmpty() && at.get(0).isPlayer()) {
                    world.equipItemOnPiece(this, at.get(0));
                    equipped = true;
                }
            }
            if (!equipped) setLocation(homeX, homeY);
        }
    }

    public Item getItem() { return item; }
}
