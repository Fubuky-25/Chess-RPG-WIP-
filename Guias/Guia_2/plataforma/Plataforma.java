import greenfoot.*;

/**
 * Plataforma — superficie solida sobre la que el Jugador se apoya.
 *
 * La plataforma no tiene comportamiento propio (no tiene act()): solo
 * existe y ocupa un rectangulo. Es Jugador quien pregunta "hay una
 * plataforma bajo mis pies?" en cada act(). Mantener la logica en un solo
 * lado evita que dos clases intenten resolver la misma colision.
 *
 * IMAGEN PROVISIONAL: mientras no exista images/plataforma.png, el sprite
 * se dibuja por codigo. Ver crearImagenConTile() mas abajo: cuando llegue
 * el tile, cambiar la linea marcada en el constructor y listo, no hay que
 * tocar nada mas del juego.
 */
public class Plataforma extends Actor
{
    private static final String ARCHIVO_TILE = "plataforma.png";

    // Colores del bloque provisional (estilo bloque de Mega Man).
    private static final Color COLOR_RELLENO = new Color(60, 80, 140);
    private static final Color COLOR_BORDE = new Color(150, 190, 240);
    private static final Color COLOR_SOMBRA = new Color(30, 40, 80);

    private static final int ALTO_BORDE = 4;

    private final int ancho;
    private final int alto;

    /** Plataforma con tamano por defecto (120 x 20). */
    public Plataforma()
    {
        this(120, 20);
    }

    /**
     * @param ancho ancho de la plataforma en pixeles.
     * @param alto  alto de la plataforma en pixeles.
     */
    public Plataforma(int ancho, int alto)
    {
        this.ancho = Math.max(1, ancho);
        this.alto = Math.max(1, alto);

        // <<< CAMBIAR AQUI cuando exista images/plataforma.png:
        //     setImage(crearImagenConTile());
        setImage(crearImagenProvisional());
    }

    /** Bloque dibujado por codigo: relleno, borde claro arriba y sombra abajo. */
    private GreenfootImage crearImagenProvisional()
    {
        GreenfootImage imagen = new GreenfootImage(ancho, alto);

        imagen.setColor(COLOR_RELLENO);
        imagen.fill();

        imagen.setColor(COLOR_BORDE);
        imagen.fillRect(0, 0, ancho, Math.min(ALTO_BORDE, alto));

        imagen.setColor(COLOR_SOMBRA);
        imagen.fillRect(0, alto - Math.min(ALTO_BORDE, alto), ancho, Math.min(ALTO_BORDE, alto));

        return imagen;
    }

    /**
     * Arma la plataforma repitiendo un tile cuadrado hasta cubrir el
     * ancho y el alto pedidos. Asi una misma imagen chica sirve para
     * plataformas de cualquier largo.
     *
     * Todavia no se usa: requiere que exista images/plataforma.png.
     */
    private GreenfootImage crearImagenConTile()
    {
        GreenfootImage tile = new GreenfootImage(ARCHIVO_TILE);
        GreenfootImage imagen = new GreenfootImage(ancho, alto);

        for (int y = 0; y < alto; y += tile.getHeight())
        {
            for (int x = 0; x < ancho; x += tile.getWidth())
            {
                imagen.drawImage(tile, x, y);
            }
        }

        return imagen;
    }
}
