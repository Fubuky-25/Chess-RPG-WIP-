import greenfoot.*;

public class Bala extends Actor
{
    private static final String ARCHIVO_IMAGEN = "bala.png";

    private static final int VELOCIDAD = 12;

    private static final int ESCALA = 3;
    private static final int ANCHO = 6 * ESCALA;
    private static final int ALTO = 4 * ESCALA;

    private static final Color COLOR_BORDE = new Color(0, 160, 240);
    private static final Color COLOR_CENTRO = new Color(220, 250, 255);
    private final int direccion;

    public Bala(int direccion)
    {
        this.direccion = direccion >= 0 ? 1 : -1;

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
    private boolean llegoAlBorde()
    {
        return getX() <= ANCHO / 2
            || getX() >= getWorld().getWidth() - 1 - ANCHO / 2;
    }
    private GreenfootImage crearImagenProvisional()
    {
        GreenfootImage imagen = new GreenfootImage(ANCHO, ALTO);

        imagen.setColor(COLOR_BORDE);
        imagen.fillOval(0, 0, ANCHO, ALTO);

        imagen.setColor(COLOR_CENTRO);
        imagen.fillOval(ANCHO / 4, ALTO / 4, ANCHO / 2, ALTO / 2);

        return imagen;
    }
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
