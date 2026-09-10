import greenfoot.*;

/** Carta de recompensa entre niveles. Se elige con un clic. */
public class RewardCard extends Actor
{
    public static final String KIND_ITEM  = "item";
    public static final String KIND_PIECE = "piece";
    public static final String KIND_HEAL  = "heal";

    private String kind;
    private Item item;        // si kind = item
    private Piece piece;      // si kind = piece
    private String title;
    private String subtitle;

    public RewardCard(String kind, Item item, Piece piece, String title, String subtitle)
    {
        this.kind = kind;
        this.item = item;
        this.piece = piece;
        this.title = title;
        this.subtitle = subtitle;
        redraw();
    }

    private void redraw()
    {
        int W = 150, H = 200;
        GreenfootImage img = new GreenfootImage(W, H);
        img.setColor(new Color(22, 22, 32, 245));
        img.fillRect(0, 0, W, H);
        img.setColor(new Color(215, 180, 90));
        img.drawRect(0, 0, W - 1, H - 1);
        img.drawRect(3, 3, W - 7, H - 7);

        int iconSize = 48;
        int ix = (W - iconSize) / 2;
        if (item != null) {
            GreenfootImage ic = item.createIcon();
            ic.scale(iconSize, iconSize);
            img.drawImage(ic, ix, 20);
        } else if (piece != null) {
            GreenfootImage sp = PieceThemeManager.getSprite(piece.getPieceType(), true);
            sp.scale(iconSize, iconSize);
            img.drawImage(sp, ix, 20);
        } else {
            GreenfootImage cross = new GreenfootImage(iconSize, iconSize);
            cross.setColor(new Color(60, 170, 90));
            cross.fillRect(iconSize / 2 - 6, 2, 12, iconSize - 4);
            cross.fillRect(2, iconSize / 2 - 6, iconSize - 4, 12);
            img.drawImage(cross, ix, 20);
        }

        drawWrapped(img, title,    10, 82,  W - 20, 13, new Color(255, 255, 255));
        drawWrapped(img, subtitle, 10, 128, W - 20, 11, new Color(200, 200, 210));

        img.setColor(new Color(150, 150, 160));
        TextUtil.drawFitted(img, "clic para elegir", 10, H - 22, W - 20, 11, 8,
                            new Color(150, 150, 160));

        setImage(img);
    }

    /** Parte el texto en lineas que caben en maxWidth (maximo 3 lineas). */
    private void drawWrapped(GreenfootImage img, String text, int x, int y, int maxWidth,
                             int size, Color color)
    {
        if (text == null) return;
        String rest = text;
        int line = 0;
        while (rest.length() > 0 && line < 3) {
            int cut = rest.length();
            while (cut > 1 && TextUtil.widthOf(rest.substring(0, cut), size) > maxWidth) {
                int space = rest.lastIndexOf(' ', cut - 1);
                cut = (space > 0) ? space : cut - 1;
            }
            String piece = rest.substring(0, cut).trim();
            if (piece.length() == 0) break;
            TextUtil.drawFitted(img, piece, x, y + line * (size + 4), maxWidth, size, 8, color);
            rest = rest.substring(cut).trim();
            line++;
        }
    }

    public void act()
    {
        World w = getWorld();
        if (!(w instanceof ChessWorld)) return;
        ChessWorld world = (ChessWorld) w;
        if (world.getState() != GameState.REWARD) return;

        if (Greenfoot.mouseClicked(this)) {
            world.chooseReward(this);
        }
    }

    public String getKind()  { return kind; }
    public Item getItem()    { return item; }
    public Piece getPiece()  { return piece; }
    public String getTitle() { return title; }
}
