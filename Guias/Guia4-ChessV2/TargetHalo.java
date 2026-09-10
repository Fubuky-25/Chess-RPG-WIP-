import greenfoot.*;

/**
 * Halo palpitante bajo la pieza que va a recibir el golpe.
 *
 * Adaptado de IndicadorObjetivo del proyecto JuegoTurnos. Responde "¿a quien
 * estoy golpeando?" mientras la barra de timing tapa parte del tablero: sin el,
 * el minijuego se juega sin saber sobre que pieza recaen las consecuencias.
 *
 * Se dibuja por debajo de Piece (ver setPaintOrder en ChessWorld) y ocupa
 * exactamente una celda, para no interferir con ninguna consulta del tablero.
 */
public class TargetHalo extends Actor
{
    private static final int PERIOD = 34;

    private boolean playerTarget;
    private int pulse = 0;

    public TargetHalo(boolean playerTarget)
    {
        this.playerTarget = playerTarget;
        redraw();
    }

    public void act()
    {
        pulse = (pulse + 1) % PERIOD;
        redraw();
    }

    private void redraw()
    {
        int cell = ChessWorld.CELL_SIZE;

        // Onda triangular 0..1..0: el halo respira en vez de parpadear.
        double half = PERIOD / 2.0;
        double t = (pulse < half) ? (pulse / half) : ((PERIOD - pulse) / half);

        int alpha = 70 + (int) Math.round(85 * t);
        int grow  = (int) Math.round(6 * t);

        GreenfootImage img = new GreenfootImage(cell, cell);
        Color c = playerTarget ? new Color(90, 170, 255, alpha)
                               : new Color(255, 210, 60, alpha);
        img.setColor(c);
        int margin = 8 - grow;
        img.fillOval(margin, margin, cell - margin * 2, cell - margin * 2);
        setImage(img);
    }
}
