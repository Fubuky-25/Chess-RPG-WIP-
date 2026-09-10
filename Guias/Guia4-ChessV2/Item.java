import greenfoot.*;

/**
 * Clase base de todo el equipamiento.
 * POLIMORFISMO: cada subclase decide en applyTo() como altera los stats de la pieza.
 */
public abstract class Item
{
    public static final String SLOT_WEAPON = "weapon";
    public static final String SLOT_ARMOR  = "armor";
    public static final String SLOT_RELIC  = "relic";

    protected String name;
    protected String description;

    public Item(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    /** En que slot de la pieza se coloca. */
    public abstract String getSlot();

    /** Modifica los stats de la pieza. Lo llama Piece.recalculateStats(). */
    public abstract void applyTo(Piece piece);

    /** Icono dibujado por codigo (no hay sprites de items en images/). */
    public abstract GreenfootImage createIcon();

    public String getName()        { return name; }
    public String getDescription() { return description; }

    /** Marco comun para todos los iconos. */
    protected GreenfootImage baseIcon(Color fill, String letter)
    {
        int s = ChessWorld.CELL_SIZE - 8;      // cabe siempre dentro de una celda
        GreenfootImage img = new GreenfootImage(s, s);
        img.setColor(new Color(20, 20, 28, 240));
        img.fillRect(0, 0, s, s);
        img.setColor(fill);
        img.fillRect(3, 3, s - 6, s - 6);
        img.setColor(new Color(240, 230, 190));
        img.drawRect(3, 3, s - 7, s - 7);
        TextUtil.drawFitted(img, letter, s / 2 - 8, s / 2 - 13, s - 8, 24, 12, Color.WHITE);
        return img;
    }
}
