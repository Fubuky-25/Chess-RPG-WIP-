import greenfoot.*;

public class Mundo extends World
{
    private static final int ANCHO_MUNDO = 800;
    private static final int ALTO_MUNDO = 500;

    private static final int ALTO_SUELO = 60;
    private static final int SUPERFICIE_SUELO = 440;

    private static final int X_LLEGADA = 60;
    private static final int Y_LLEGADA = 0;

    private GreenfootSound musicaDeFondo;

    public Mundo()
    {
        super(ANCHO_MUNDO, ALTO_MUNDO, 1);
        
        musicaDeFondo = new GreenfootSound("musica.mp3");
        
        prepararMundo();
    }

    @Override
    public void started()
    {
        if (musicaDeFondo != null)
        {
            musicaDeFondo.playLoop();
        }
    }

    @Override
    public void stopped()
    {
        if (musicaDeFondo != null)
        {
            musicaDeFondo.pause();
        }
    }

    private void prepararMundo()
    {
        crearSuelo();
        crearPlataformas();
        crearJugador();
    }

    private void crearSuelo()
    {
        addObject(
            new Plataforma(ANCHO_MUNDO, ALTO_SUELO),
            ANCHO_MUNDO / 2,
            SUPERFICIE_SUELO + ALTO_SUELO / 2);
    }

    private void crearPlataformas()
    {
        addObject(new Plataforma(150, 20), 180, 370);
        addObject(new Plataforma(150, 20), 390, 300);
        addObject(new Plataforma(100, 20), 300, 210);
        addObject(new Plataforma(140, 20), 600, 235);
        addObject(new Plataforma(120, 20), 740, 160);
    }

    private void crearJugador()
    {
        addObject(new Jugador(), X_LLEGADA, Y_LLEGADA);
    }
}