import greenfoot.*;

/**
 * Cartel que late con la fase actual: DESPLIEGUE, TU TURNO, COMBATE, TURNO IA...
 *
 * Adaptado de IndicadorTurno del proyecto JuegoTurnos. El pulso no es adorno:
 * responde "¿me toca a mi?" antes de que el jugador tenga que deducirlo
 * probando clics. Cuando NO es su turno el cartel se queda fijo y apagado, que
 * es la misma senal al reves.
 *
 * La imagen base se construye solo cuando cambia el texto; el pulso se limita a
 * cambiar la transparencia de una copia, y solo en el ciclo en que cruza el
 * umbral. Asi el latido no cuesta un redibujado por ciclo.
 */
public class TurnIndicator extends Actor
{
    private static final int W = 208;
    private static final int H = 36;
    private static final int PERIOD = 30;

    private String text = "";
    private Color color = Color.WHITE;
    private boolean pulsing = false;

    private GreenfootImage base;
    private int pulse = 0;
    private boolean dimmed = false;

    public TurnIndicator()
    {
        rebuild();
    }

    public void act()
    {
        if (!pulsing) return;

        pulse = (pulse + 1) % PERIOD;
        boolean wantDim = pulse >= PERIOD / 2;
        if (wantDim != dimmed) {
            dimmed = wantDim;
            applyPulse();
        }
    }

    /**
     * @param pulsing true solo cuando el juego espera una accion del jugador.
     */
    public void show(String text, Color color, boolean pulsing)
    {
        boolean sameText = (text == null) ? (this.text == null) : text.equals(this.text);
        if (sameText && color == this.color && pulsing == this.pulsing) return;

        this.text = text;
        this.color = color;
        this.pulsing = pulsing;
        this.pulse = 0;
        this.dimmed = false;
        rebuild();
    }

    private void rebuild()
    {
        base = new GreenfootImage(W, H);
        base.setColor(new Color(18, 18, 26, 235));
        base.fillRect(0, 0, W, H);
        base.setColor(new Color(215, 180, 90));
        base.drawRect(0, 0, W - 1, H - 1);
        TextUtil.drawFitted(base, text, 10, 9, W - 20, 16, 9, color);
        applyPulse();
    }

    private void applyPulse()
    {
        GreenfootImage img = new GreenfootImage(base);
        if (!pulsing)      img.setTransparency(170);
        else if (dimmed)   img.setTransparency(200);
        else               img.setTransparency(255);
        setImage(img);
    }
}
