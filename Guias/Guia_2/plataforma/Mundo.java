import greenfoot.*;

/**
 * Mundo — escenario del juego (800 x 500, como en la seccion 6 de la
 * guia). El fondo es images/Background.png, configurado desde Greenfoot.
 *
 * Responsabilidad: armar el nivel. Toda la fisica vive en Jugador; aqui
 * solo se decide donde va cada cosa.
 *
 * Diseno del nivel: el suelo es una Plataforma mas (ancha y al fondo), no
 * un caso especial. Asi el jugador aterriza siempre con el mismo codigo,
 * este cayendo sobre el piso o sobre una plataforma flotante.
 *
 * Alturas: con IMPULSO_SALTO = -14 y GRAVEDAD = 0.8, el salto sube unos
 * 115 px y avanza unos 175 px en horizontal. Cada escalon de este nivel
 * sube 65-90 px y deja huecos de 60-65 px, o sea que todo es alcanzable
 * con margen, y los huecos son lo bastante anchos como para caerse por
 * ellos.
 */
public class Mundo extends World
{
    private static final int ANCHO_MUNDO = 800;
    private static final int ALTO_MUNDO = 500;

    /** Alto del bloque de suelo; su cara superior queda en y = 440. */
    private static final int ALTO_SUELO = 60;
    private static final int SUPERFICIE_SUELO = 440;

    /** Mitad del alto de la imagen del jugador (90 / 2), para pararlo en el suelo. */
    private static final int MEDIO_JUGADOR = 45;

    public Mundo()
    {
        super(ANCHO_MUNDO, ALTO_MUNDO, 1);
        prepararMundo();
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

    /** Escalera de plataformas con huecos por donde se puede caer. */
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
        // Se ubica por el centro del actor, asi que hay que restar medio
        // sprite para que los pies queden justo sobre el suelo.
        addObject(new Jugador(), 100, SUPERFICIE_SUELO - MEDIO_JUGADOR);
    }
}
