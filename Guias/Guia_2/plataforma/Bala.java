import greenfoot.*;

/**
 * Bala — proyectil del Mega Buster.
 *
 * Responsabilidad unica: avanzar en linea recta y desaparecer al llegar
 * al borde del escenario. No sabe quien la disparo ni contra que choca;
 * si mas adelante se agregan enemigos, la deteccion del impacto se agrega
 * aqui (o en el enemigo), sin tocar al Jugador.
 *
 * Quien controla cuantas balas existen es Jugador (MAXIMO_BALAS): la bala
 * solo se preocupa de eliminarse cuando ya no sirve, para que ese cupo
 * quede libre otra vez.
 *
 * IMAGEN PROVISIONAL: mientras no exista images/bala.png, el disparo se
 * dibuja por codigo. Ver la linea marcada en el constructor.
 */
public class Bala extends Actor
{
    private static final String ARCHIVO_IMAGEN = "bala.png";

    /** Debe ser mayor que la velocidad del jugador para que se despegue. */
    private static final int VELOCIDAD = 12;

    private static final int ESCALA = 3;
    private static final int ANCHO = 6 * ESCALA;
    private static final int ALTO = 4 * ESCALA;

    private static final Color COLOR_BORDE = new Color(0, 160, 240);
    private static final Color COLOR_CENTRO = new Color(220, 250, 255);

    /** 1 = hacia la derecha, -1 = hacia la izquierda. */
    private final int direccion;

    public Bala(int direccion)
    {
        this.direccion = direccion >= 0 ? 1 : -1;

        // <<< CAMBIAR AQUI cuando exista images/bala.png:
        //     setImage(cargarImagenDeArchivo());
        setImage(crearImagenProvisional());
    }

    public void act()
    {
        setLocation(getX() + direccion * VELOCIDAD, getY());

        if (llegoAlBorde())
        {
            getWorld().removeObject(this);
        }
    }

    /**
     * Greenfoot no deja que un actor salga del mundo: al llegar al borde
     * queda pegado ahi. Por eso la condicion es "estoy tocando el borde",
     * no "estoy fuera del mundo".
     */
    private boolean llegoAlBorde()
    {
        return getX() <= ANCHO / 2
            || getX() >= getWorld().getWidth() - 1 - ANCHO / 2;
    }

    /** Disparo dibujado por codigo: nucleo claro con halo celeste. */
    private GreenfootImage crearImagenProvisional()
    {
        GreenfootImage imagen = new GreenfootImage(ANCHO, ALTO);

        imagen.setColor(COLOR_BORDE);
        imagen.fillOval(0, 0, ANCHO, ALTO);

        imagen.setColor(COLOR_CENTRO);
        imagen.fillOval(ANCHO / 4, ALTO / 4, ANCHO / 2, ALTO / 2);

        return imagen;
    }

    /**
     * Carga bala.png y la escala igual que al personaje, y la refleja si
     * el disparo va hacia la izquierda.
     *
     * Todavia no se usa: requiere que exista images/bala.png.
     */
    private GreenfootImage cargarImagenDeArchivo()
    {
        GreenfootImage imagen = new GreenfootImage(ARCHIVO_IMAGEN);
        imagen.scale(imagen.getWidth() * ESCALA, imagen.getHeight() * ESCALA);

        if (direccion < 0)
        {
            imagen.mirrorHorizontally();
        }

        return imagen;
    }
}
