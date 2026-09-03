import greenfoot.*;

public class JuegoWorld extends World
{
    // grosor de los muros de la imagen de fondo: define el piso jugable
    public static final int MURO_LATERAL = 42;
    public static final int MURO_SUPERIOR = 50;
    public static final int MURO_INFERIOR = 48;

    private Jugador jugador;
    private GestorOleadas gestor;
    private HUD hud;
    private boolean terminado = false;

    public JuegoWorld()
    {
        super(900, 600, 1);
        setBackground(construirFondo());
        setPaintOrder(HUD.class, Bala.class, Jugador.class, SoldadoEnemigo.class);

        jugador = new Jugador();
        addObject(jugador, getWidth() / 2, getHeight() / 2);

        gestor = new GestorOleadas(this, jugador);
        hud = new HUD(this, jugador, gestor);
        addObject(hud, 210, 26);
    }

    public void act()
    {
        if (terminado) return;
        gestor.actualizar();
    }

    public void gameOver()
    {
        terminado = true;
        showText("GAME OVER - llegaste a la oleada " + gestor.getOleada(),
                 getWidth() / 2, getHeight() / 2);
        Greenfoot.stop();
    }

    public int limitarX(int x, int margen)
    {
        return Math.max(MURO_LATERAL + margen,
               Math.min(getWidth() - MURO_LATERAL - margen, x));
    }

    public int limitarY(int y, int margen)
    {
        return Math.max(MURO_SUPERIOR + margen,
               Math.min(getHeight() - MURO_INFERIOR - margen, y));
    }

    public boolean dentroDeLaSala(int x, int y)
    {
        return x > MURO_LATERAL && x < getWidth() - MURO_LATERAL
            && y > MURO_SUPERIOR && y < getHeight() - MURO_INFERIOR;
    }

    /**
     * El fondo es 16:9 y el mundo no. Se escala a lo ancho sin deformar y el alto
     * que falta se rellena repitiendo piso, dejando el muro inferior abajo del todo.
     */
    private GreenfootImage construirFondo()
    {
        GreenfootImage sala = new GreenfootImage("Fondo.jpg");
        int alto = getWidth() * sala.getHeight() / sala.getWidth();
        sala.scale(getWidth(), alto);

        GreenfootImage lienzo = new GreenfootImage(getWidth(), getHeight());
        lienzo.drawImage(sala, 0, 0);

        int altoInferior = getHeight() - alto + 60;
        GreenfootImage inferior = new GreenfootImage(getWidth(), altoInferior);
        inferior.drawImage(sala, 0, -(alto - altoInferior));
        lienzo.drawImage(inferior, 0, getHeight() - altoInferior);

        return lienzo;
    }
}
