import greenfoot.*;

/**
 * Restos de una pieza capturada: se encoge y se desvanece.
 *
 * La pieza real sale del mundo en el mismo instante en que muere (la logica no
 * puede esperar a una animacion), asi que lo que se ve aqui es solo una copia
 * de su ultima imagen. Es puramente decorativo: no ocupa casilla ni consume
 * turno, y por eso la pieza atacante puede entrar en la casilla por encima.
 */
public class DeathFx extends Actor
{
    private static final int LIFE = 26;

    private GreenfootImage sprite;
    private int life = 0;

    public DeathFx(GreenfootImage lastImage)
    {
        sprite = (lastImage == null)
               ? new GreenfootImage(ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE)
               : new GreenfootImage(lastImage);
        redraw();
    }

    public void act()
    {
        life++;
        if (life >= LIFE) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }
        redraw();
    }

    private void redraw()
    {
        int cell = ChessWorld.CELL_SIZE;
        double t = (double) life / (double) LIFE;

        int size = (int) Math.round(cell * (1.0 - 0.55 * t));
        if (size < 6) size = 6;

        GreenfootImage copy = new GreenfootImage(sprite);
        copy.scale(size, size);
        int alpha = (int) Math.round(235 * (1.0 - t));
        if (alpha < 0) alpha = 0;
        copy.setTransparency(alpha);

        GreenfootImage img = new GreenfootImage(cell, cell);
        img.drawImage(copy, (cell - size) / 2, (cell - size) / 2);
        setImage(img);
    }
}
