import greenfoot.*;

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

    public Plataforma()
    {
        this(120, 20);
    }


    public Plataforma(int ancho, int alto)
    {
        this.ancho = Math.max(1, ancho);
        this.alto = Math.max(1, alto);
        setImage(crearImagenProvisional());
    }


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
