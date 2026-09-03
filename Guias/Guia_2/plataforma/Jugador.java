import greenfoot.*;

public class Jugador extends Actor
{
    private enum Estado
    {
        QUIETO,
        CAMINANDO_DERECHA,
        CAMINANDO_IZQUIERDA,
        SALTANDO,
        CAYENDO,
        LLEGANDO,
        APARECIENDO
    }

    private static final int ESCALA = 3;

    private static final int ANCHO_IMAGEN = 42 * ESCALA;   
    private static final int ALTO_IMAGEN = 32 * ESCALA;    
    private static final int ANCHO_CAJA = 14 * ESCALA;     
    private static final int CUADROS_POR_FRAME = 6;
    private static final int CUADROS_POR_WARP = 8;
    private static final int CUADROS_POSE_DISPARO = 16;

    private static final int DISTANCIA_CANON = 15 * ESCALA;
    private static final int ALTURA_CANON = 2 * ESCALA;

    private static final double GRAVEDAD = 0.8;
    private static final double IMPULSO_SALTO = -14.0;      
    private static final double CAIDA_MAXIMA = 16.0;        
    private static final int VELOCIDAD_CAMINATA = 5;

    private static final int MAXIMO_BALAS = 3;

    private Estado estado = Estado.LLEGANDO;

    private double vy = 0;
    private double yReal;

    private boolean enSuelo = false;
    private boolean mirandoIzquierda = false;

    private boolean pidiendoDerecha = false;
    private boolean pidiendoIzquierda = false;

    private boolean teclaDisparoAntes = false;

    private int frameActual = 0;
    private int contadorAnimacion = 0;

    private int cuadrosDisparando = 0;

    private int frameWarp = 0;
    private int contadorWarp = 0;

    private GreenfootImage quietoDerecha;
    private GreenfootImage quietoIzquierda;
    private GreenfootImage saltoDerecha;
    private GreenfootImage saltoIzquierda;
    private GreenfootImage[] caminarDerecha;
    private GreenfootImage[] caminarIzquierda;

    private GreenfootImage disparoQuietoDerecha;
    private GreenfootImage disparoQuietoIzquierda;
    private GreenfootImage disparoSaltoDerecha;
    private GreenfootImage disparoSaltoIzquierda;
    private GreenfootImage[] disparoCaminarDerecha;
    private GreenfootImage[] disparoCaminarIzquierda;

    private GreenfootImage[] warp;

    public Jugador()
    {
        cargarImagenes();
    }

    protected void addedToWorld(World mundo)
    {
        yReal = getY();
    }

    private void cargarImagenes()
    {
        quietoIzquierda = prepararSprite("idle.png");
        quietoDerecha = espejo(quietoIzquierda);

        saltoIzquierda = prepararSprite("jump.png");
        saltoDerecha = espejo(saltoIzquierda);

        caminarIzquierda = new GreenfootImage[4];
        caminarIzquierda[0] = prepararSprite("walk1.png");
        caminarIzquierda[1] = prepararSprite("walk2.png");
        caminarIzquierda[2] = prepararSprite("walk3.png");
        caminarIzquierda[3] = prepararSprite("walk4.png");

        caminarDerecha = new GreenfootImage[caminarIzquierda.length];

        for (int i = 0; i < caminarIzquierda.length; i++)
        {
            caminarDerecha[i] = espejo(caminarIzquierda[i]);
        }

        disparoQuietoIzquierda = prepararSprite("shooting_stand.png", 0);
        disparoQuietoDerecha = espejo(disparoQuietoIzquierda);

        disparoSaltoIzquierda = prepararSprite("9.png", -1);
        disparoSaltoDerecha = espejo(disparoSaltoIzquierda);

        disparoCaminarIzquierda = new GreenfootImage[4];
        disparoCaminarIzquierda[0] = prepararSprite("shooting_walk1.png", -2);
        disparoCaminarIzquierda[1] = prepararSprite("shooting_walk2.png", -5);
        disparoCaminarIzquierda[2] = prepararSprite("shooting_walk3.png", -5);
        disparoCaminarIzquierda[3] = prepararSprite("shooting_walk4.png", -5);

        disparoCaminarDerecha = new GreenfootImage[disparoCaminarIzquierda.length];

        for (int i = 0; i < disparoCaminarIzquierda.length; i++)
        {
            disparoCaminarDerecha[i] = espejo(disparoCaminarIzquierda[i]);
        }

        warp = new GreenfootImage[3];
        warp[0] = prepararSprite("warp1.png");
        warp[1] = prepararSprite("warp2.png");
        warp[2] = prepararSprite(unirWarpFinal(), 0);

        setImage(warp[0]);
    }

    private GreenfootImage unirWarpFinal()
    {
        GreenfootImage arriba = new GreenfootImage("warp3B.png");
        GreenfootImage abajo = new GreenfootImage("warp3A.png");

        int ancho = Math.max(arriba.getWidth(), abajo.getWidth());
        int alto = arriba.getHeight() + abajo.getHeight();

        GreenfootImage junta = new GreenfootImage(ancho, alto);
        junta.drawImage(arriba, (ancho - arriba.getWidth()) / 2, 0);
        junta.drawImage(abajo, (ancho - abajo.getWidth()) / 2, arriba.getHeight());

        return junta;
    }

    private GreenfootImage prepararSprite(String archivo)
    {
        return prepararSprite(new GreenfootImage(archivo), 0);
    }

    private GreenfootImage prepararSprite(String archivo, int desplazamiento)
    {
        return prepararSprite(new GreenfootImage(archivo), desplazamiento);
    }

    private GreenfootImage prepararSprite(GreenfootImage original, int desplazamiento)
    {
        int ancho = original.getWidth() * ESCALA;
        int alto = original.getHeight() * ESCALA;
        original.scale(ancho, alto);

        GreenfootImage lienzo = new GreenfootImage(ANCHO_IMAGEN, ALTO_IMAGEN);
        lienzo.drawImage(
            original,
            (ANCHO_IMAGEN - ancho) / 2 + desplazamiento * ESCALA,
            ALTO_IMAGEN - alto);

        return lienzo;
    }

    private GreenfootImage espejo(GreenfootImage imagen)
    {
        GreenfootImage copia = new GreenfootImage(imagen);
        copia.mirrorHorizontally();
        return copia;
    }

    public void act()
    {
        if (estado == Estado.LLEGANDO)
        {
            aplicarGravedad();

            if (enSuelo)
            {
                estado = Estado.APARECIENDO;
                frameWarp = 0;
                contadorWarp = 0;
            }

            actualizarAnimacion();
            return;
        }

        if (estado == Estado.APARECIENDO)
        {
            avanzarAparicion();
            actualizarAnimacion();
            return;
        }

        controlarMovimientoHorizontal();
        controlarSalto();
        aplicarGravedad();
        actualizarEstado();
        controlarDisparo();
        actualizarAnimacion();
    }

    private void controlarMovimientoHorizontal()
    {
        pidiendoDerecha = Greenfoot.isKeyDown("right");
        pidiendoIzquierda = Greenfoot.isKeyDown("left");

        if (pidiendoDerecha && !pidiendoIzquierda)
        {
            moverEnX(VELOCIDAD_CAMINATA);
            mirandoIzquierda = false;
        }
        else if (pidiendoIzquierda && !pidiendoDerecha)
        {
            moverEnX(-VELOCIDAD_CAMINATA);
            mirandoIzquierda = true;
        }
    }

    private void moverEnX(int desplazamiento)
    {
        int nuevaX = getX() + desplazamiento;

        int minimo = ANCHO_CAJA / 2;
        int maximo = getWorld().getWidth() - ANCHO_CAJA / 2;

        if (nuevaX < minimo)
        {
            nuevaX = minimo;
        }
        else if (nuevaX > maximo)
        {
            nuevaX = maximo;
        }

        setLocation(nuevaX, getY());
    }

    private void controlarSalto()
    {
        if (Greenfoot.isKeyDown("x") && enSuelo)
        {
            vy = IMPULSO_SALTO;
            enSuelo = false;
        }
    }

    private void aplicarGravedad()
    {
        vy += GRAVEDAD;

        if (vy > CAIDA_MAXIMA)
        {
            vy = CAIDA_MAXIMA;
        }

        double piesAntes = pies();
        double piesDespues = piesAntes + vy;

        enSuelo = false;

        if (vy >= 0)
        {
            Plataforma soporte = buscarSoporte(piesAntes, piesDespues);

            if (soporte != null)
            {
                piesDespues = superficieDe(soporte);
                vy = 0;
                enSuelo = true;
            }
        }

        ubicarPies(piesDespues);
    }

    private Plataforma buscarSoporte(double piesAntes, double piesDespues)
    {
        Plataforma elegida = null;
        double mejorSuperficie = 0;

        for (Plataforma plataforma : getWorld().getObjects(Plataforma.class))
        {
            if (!haySuperposicionHorizontal(plataforma))
            {
                continue;
            }

            double superficie = superficieDe(plataforma);

            boolean cruzaLaSuperficie =
                superficie >= piesAntes - 1 && superficie <= piesDespues;

            if (!cruzaLaSuperficie)
            {
                continue;
            }

            if (elegida == null || superficie < mejorSuperficie)
            {
                elegida = plataforma;
                mejorSuperficie = superficie;
            }
        }

        return elegida;
    }

    private boolean haySuperposicionHorizontal(Plataforma plataforma)
    {
        int izquierdaJugador = getX() - ANCHO_CAJA / 2;
        int derechaJugador = getX() + ANCHO_CAJA / 2;

        int mitadPlataforma = plataforma.getImage().getWidth() / 2;
        int izquierdaPlataforma = plataforma.getX() - mitadPlataforma;
        int derechaPlataforma = plataforma.getX() + mitadPlataforma;

        return derechaJugador > izquierdaPlataforma
            && izquierdaJugador < derechaPlataforma;
    }

    private double superficieDe(Plataforma plataforma)
    {
        return plataforma.getY() - plataforma.getImage().getHeight() / 2.0;
    }

    private double pies()
    {
        return yReal + ALTO_IMAGEN / 2.0;
    }

    private void ubicarPies(double nuevaAlturaDePies)
    {
        yReal = nuevaAlturaDePies - ALTO_IMAGEN / 2.0;
        setLocation(getX(), (int) Math.round(yReal));
    }

    private void actualizarEstado()
    {
        if (!enSuelo)
        {
            estado = vy < 0 ? Estado.SALTANDO : Estado.CAYENDO;
        }
        else if (pidiendoDerecha && !pidiendoIzquierda)
        {
            estado = Estado.CAMINANDO_DERECHA;
        }
        else if (pidiendoIzquierda && !pidiendoDerecha)
        {
            estado = Estado.CAMINANDO_IZQUIERDA;
        }
        else
        {
            estado = Estado.QUIETO;
        }
    }

    private void avanzarAparicion()
    {
        contadorWarp++;

        if (contadorWarp >= CUADROS_POR_WARP)
        {
            contadorWarp = 0;
            frameWarp++;

            if (frameWarp >= warp.length)
            {
                estado = Estado.QUIETO;
            }
        }
    }

    private void controlarDisparo()
    {
        if (cuadrosDisparando > 0)
        {
            cuadrosDisparando--;
        }

        boolean teclaAhora = Greenfoot.isKeyDown("z");

        if (teclaAhora && !teclaDisparoAntes && quedanBalasDisponibles())
        {
            disparar();
        }

        teclaDisparoAntes = teclaAhora;
    }

    private boolean quedanBalasDisponibles()
    {
        return getWorld().getObjects(Bala.class).size() < MAXIMO_BALAS;
    }

    private void disparar()
    {
        int direccion = mirandoIzquierda ? -1 : 1;

        int x = getX() + direccion * DISTANCIA_CANON;
        int y = getY() + ALTURA_CANON;

        getWorld().addObject(new Bala(direccion), x, y);

        cuadrosDisparando = CUADROS_POSE_DISPARO;
    }

    private void actualizarAnimacion()
    {
        if (estado == Estado.APARECIENDO)
        {
            setImage(warp[frameWarp]);
        }
        else if (estado == Estado.LLEGANDO
            || estado == Estado.SALTANDO
            || estado == Estado.CAYENDO)
        {
            if (disparando())
            {
                setImage(mirandoIzquierda ? disparoSaltoIzquierda : disparoSaltoDerecha);
            }
            else
            {
                setImage(mirandoIzquierda ? saltoIzquierda : saltoDerecha);
            }
        }
        else if (estado == Estado.CAMINANDO_DERECHA)
        {
            animarCaminata(false);
        }
        else if (estado == Estado.CAMINANDO_IZQUIERDA)
        {
            animarCaminata(true);
        }
        else
        {
            if (disparando())
            {
                setImage(mirandoIzquierda ? disparoQuietoIzquierda : disparoQuietoDerecha);
            }
            else
            {
                setImage(mirandoIzquierda ? quietoIzquierda : quietoDerecha);
            }

            reiniciarCaminata();
        }
    }

    private boolean disparando()
    {
        return cuadrosDisparando > 0;
    }

    private void animarCaminata(boolean izquierda)
    {
        contadorAnimacion++;

        if (contadorAnimacion >= CUADROS_POR_FRAME)
        {
            frameActual = (frameActual + 1) % caminarDerecha.length;
            contadorAnimacion = 0;
        }

        GreenfootImage[] cuadros;

        if (disparando())
        {
            cuadros = izquierda ? disparoCaminarIzquierda : disparoCaminarDerecha;
        }
        else
        {
            cuadros = izquierda ? caminarIzquierda : caminarDerecha;
        }

        setImage(cuadros[frameActual]);
    }

    private void reiniciarCaminata()
    {
        frameActual = 0;
        contadorAnimacion = 0;
    }
}