import greenfoot.*;

/**
 * Texto que sube sobre la pieza golpeada y se desvanece: "-12", "CRITICO", "FALLO".
 *
 * Adaptado de TextoFlotante del proyecto JuegoTurnos. Alli el texto subia con
 * setLocation(getX(), getY()-1), pero aqui el mundo es una rejilla de celdas:
 * restar 1 a la Y saltaria una casilla entera. Por eso el movimiento se hace
 * DENTRO de la imagen del actor, que es mas alta que una celda.
 *
 * Explica el resultado cuantitativo del golpe justo donde ocurre y desaparece
 * solo para no acumular ruido en pantalla.
 */
public class FloatingText extends Actor
{
    private static final int W = 150;
    private static final int H = 96;
    private static final int LIFE = 46;
    private static final int RISE = 40;

    private GreenfootImage label;
    private int life = 0;
    private int delay;

    /**
     * @param delay ciclos de espera antes de aparecer. Sirve para que el numero
     *              salga cuando la embestida llega al objetivo, no antes.
     */
    public FloatingText(String text, Color color, int delay)
    {
        this.delay = delay;
        this.label = new GreenfootImage(text, 16, color, null);
        setImage(new GreenfootImage(W, H));
    }

    public void act()
    {
        if (delay > 0) { delay--; return; }

        life++;
        if (life >= LIFE) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }
        redraw();
    }

    private void redraw()
    {
        double t = (double) life / (double) LIFE;

        // Se desvanece solo en el ultimo 45% de su vida
        int alpha = 255;
        if (t > 0.55) alpha = (int) Math.round(255 * (1.0 - (t - 0.55) / 0.45));
        if (alpha < 0) alpha = 0;
        label.setTransparency(alpha);

        GreenfootImage img = new GreenfootImage(W, H);
        int x = (W - label.getWidth()) / 2;
        int y = H - 30 - (int) Math.round(RISE * t);
        img.drawImage(label, x, y);
        setImage(img);
    }
}
