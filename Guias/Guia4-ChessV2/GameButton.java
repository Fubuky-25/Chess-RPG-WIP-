import greenfoot.*;

/**
 * Boton generico de la interfaz. Se ajusta al ancho del texto.
 *
 * Late mientras la accion esta disponible y se apaga cuando no lo esta, igual
 * que BotonAccion del proyecto JuegoTurnos. El latido no es adorno: significa
 * "puedes pulsar esto ahora". Durante el turno de la IA, el minijuego o una
 * animacion, el boton se ve gris y deja de responder al clic, asi que el
 * jugador no descubre a base de clics fallidos que no era su turno.
 */
public class GameButton extends Actor
{
    private static final int PERIOD = 24;

    private String label;
    private String action;

    private GreenfootImage base;
    private boolean enabled = true;
    private int pulse = 0;
    private boolean dimmed = false;

    public GameButton(String label, String action)
    {
        this.label = label;
        this.action = action;
        rebuild();
    }

    public void act()
    {
        boolean nowEnabled = computeEnabled();

        if (nowEnabled != enabled) {
            enabled = nowEnabled;
            pulse = 0;
            dimmed = false;
            rebuild();
        }

        if (enabled) {
            pulse = (pulse + 1) % PERIOD;
            boolean wantDim = pulse >= PERIOD / 2;
            if (wantDim != dimmed) { dimmed = wantDim; applyPulse(); }

            if (Greenfoot.mouseClicked(this)) {
                World w = getWorld();
                if (w instanceof ChessWorld) ((ChessWorld) w).onButtonPressed(action);
            }
        }
    }

    /** Solo hay accion posible en las fases en las que decide el jugador. */
    private boolean computeEnabled()
    {
        World w = getWorld();
        if (!(w instanceof ChessWorld)) return true;
        GameState s = ((ChessWorld) w).getState();
        return s == GameState.PLAYER_TURN || s == GameState.DEPLOY;
    }

    /** Construye la imagen fija. El latido solo cambia la transparencia. */
    private void rebuild()
    {
        int textW = TextUtil.widthOf(label, 14);
        int w = Math.max(130, textW + 24);
        int h = 34;

        base = new GreenfootImage(w, h);
        base.setColor(enabled ? new Color(35, 120, 60) : new Color(70, 70, 78));
        base.fillRect(0, 0, w, h);
        base.setColor(enabled ? new Color(240, 230, 180) : new Color(140, 140, 145));
        base.drawRect(0, 0, w - 1, h - 1);
        TextUtil.drawFitted(base, label, 12, 8, w - 24, 14, 9,
                            enabled ? Color.WHITE : new Color(180, 180, 185));
        applyPulse();
    }

    private void applyPulse()
    {
        GreenfootImage img = new GreenfootImage(base);
        if (!enabled)     img.setTransparency(150);
        else if (dimmed)  img.setTransparency(205);
        else              img.setTransparency(255);
        setImage(img);
    }

    public String getAction() { return action; }
}
