import greenfoot.*;

/**
 * Crea una sola vez las imagenes del juego y las reutiliza.
 * El jugador se arma con dos piezas: cuerpo (camina) y cabeza (apunta).
 */
public class FabricaImagenes
{
    public static final int ABAJO = 0;
    public static final int ARRIBA = 1;
    public static final int IZQUIERDA = 2;
    public static final int DERECHA = 3;

    public static final int FRAMES_LATERAL = 10;
    public static final int FRAMES_VERTICAL = 9;

    private static final int ESCALA = 2;
    private static final int ANCHO = 28;
    private static final int ALTO = 36;

    // 0 quieto | 1..10 lateral derecha | 11..20 lateral izquierda | 21..29 vertical
    private static final int TOTAL_CUERPOS = 30;

    private static final GreenfootImage[] CABEZAS = cargarCabezas();
    private static final GreenfootImage[] CUERPOS = cargarCuerpos();
    private static final GreenfootImage[][] JUGADOR = new GreenfootImage[4][TOTAL_CUERPOS];

    private static final GreenfootImage[] ENEMIGO = cargarEnemigo(false);
    private static final GreenfootImage[] ENEMIGO_GOLPEADO = cargarEnemigo(true);
    private static final GreenfootImage BALA = crearLagrima();

    public static int cuerpoQuieto()
    {
        return 0;
    }

    public static int cuerpoLateral(int frame, boolean haciaDerecha)
    {
        int base = haciaDerecha ? 1 : 1 + FRAMES_LATERAL;
        return base + frame % FRAMES_LATERAL;
    }

    public static int cuerpoVertical(int frame)
    {
        return 1 + 2 * FRAMES_LATERAL + frame % FRAMES_VERTICAL;
    }

    /** Devuelve la imagen ya compuesta; la crea solo la primera vez que se pide. */
    public static GreenfootImage jugador(int dirCabeza, int cuerpo)
    {
        if (JUGADOR[dirCabeza][cuerpo] == null)
        {
            JUGADOR[dirCabeza][cuerpo] = componer(dirCabeza, cuerpo);
        }
        return JUGADOR[dirCabeza][cuerpo];
    }

    public static GreenfootImage enemigo(int frame)
    {
        return ENEMIGO[frame % ENEMIGO.length];
    }

    public static GreenfootImage enemigoGolpeado(int frame)
    {
        return ENEMIGO_GOLPEADO[frame % ENEMIGO_GOLPEADO.length];
    }

    public static GreenfootImage bala()
    {
        return BALA;
    }

    private static GreenfootImage componer(int dirCabeza, int cuerpo)
    {
        GreenfootImage lienzo = new GreenfootImage(ANCHO, ALTO);
        GreenfootImage piernas = CUERPOS[cuerpo];
        GreenfootImage cabeza = CABEZAS[dirCabeza];

        // los pies siempre tocan el borde inferior aunque el frame cambie de alto
        lienzo.drawImage(piernas, (ANCHO - piernas.getWidth()) / 2, ALTO - piernas.getHeight());
        lienzo.drawImage(cabeza, (ANCHO - cabeza.getWidth()) / 2, 0);
        lienzo.scale(ANCHO * ESCALA, ALTO * ESCALA);
        return lienzo;
    }

    private static GreenfootImage[] cargarCabezas()
    {
        GreenfootImage[] c = new GreenfootImage[4];
        c[ABAJO] = new GreenfootImage("PJ/CabezaFrente.png");
        c[ARRIBA] = new GreenfootImage("PJ/CabezaEspalda.png");
        c[IZQUIERDA] = new GreenfootImage("PJ/CabezaIzquierda.png");
        c[DERECHA] = new GreenfootImage("PJ/CabezaDerecha.png");
        return c;
    }

    private static GreenfootImage[] cargarCuerpos()
    {
        GreenfootImage[] c = new GreenfootImage[TOTAL_CUERPOS];
        c[0] = new GreenfootImage("PJ/Standig.png");

        for (int i = 0; i < FRAMES_LATERAL; i++)
        {
            c[1 + i] = new GreenfootImage("PJ/Walk" + i + ".png");

            GreenfootImage espejo = new GreenfootImage("PJ/Walk" + i + ".png");
            espejo.mirrorHorizontally();
            c[1 + FRAMES_LATERAL + i] = espejo;
        }

        for (int i = 0; i < FRAMES_VERTICAL; i++)
        {
            c[1 + 2 * FRAMES_LATERAL + i] = new GreenfootImage("PJ/WalkUp_Down" + i + ".png");
        }
        return c;
    }

    private static GreenfootImage[] cargarEnemigo(boolean blanco)
    {
        GreenfootImage[] c = new GreenfootImage[2];
        for (int i = 0; i < c.length; i++)
        {
            GreenfootImage original = new GreenfootImage("Monster/" + i + ".png");
            GreenfootImage lienzo = new GreenfootImage(28, 30);
            lienzo.drawImage(original,
                             (28 - original.getWidth()) / 2,
                             30 - original.getHeight());
            lienzo.scale(28 * ESCALA, 30 * ESCALA);
            if (blanco) blanquear(lienzo);
            c[i] = lienzo;
        }
        return c;
    }

    /** Aclara solo los pixeles visibles: sirve como destello al recibir un impacto. */
    private static void blanquear(GreenfootImage img)
    {
        for (int x = 0; x < img.getWidth(); x++)
        {
            for (int y = 0; y < img.getHeight(); y++)
            {
                Color c = img.getColorAt(x, y);
                if (c.getAlpha() > 0)
                {
                    img.setColorAt(x, y, new Color(255, 235, 235, c.getAlpha()));
                }
            }
        }
    }

    private static GreenfootImage crearLagrima()
    {
        GreenfootImage img = new GreenfootImage(7, 7);          // <<< CAMBIAR AQUI si llega un sprite de bala
        img.setColor(new Color(120, 190, 230));
        img.fillOval(0, 0, 7, 7);
        img.setColor(new Color(235, 250, 255));
        img.fillOval(1, 1, 3, 3);
        img.scale(7 * ESCALA, 7 * ESCALA);
        return img;
    }
}
