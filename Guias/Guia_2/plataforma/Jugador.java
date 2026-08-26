import greenfoot.*;

/**
 * Jugador — personaje controlable (sprites de Mega Man).
 *
 * Flujo por cada act():
 *   Entrada (teclado) -> Estado (enum Estado) -> Fisica (gravedad y salto)
 *   -> Colision con Plataforma -> Representacion visual (sprite mostrado).
 *
 * Cambio importante respecto a la version de la guia (seccion 10):
 * la guia calcula la altura con la ecuacion parabolica y(t) = y0 - v0*t +
 * 0.5*g*t^2, es decir, la caida SOLO existe mientras dura un salto. Aqui
 * la gravedad es continua: cada act() se acumula velocidad vertical
 * (vy += GRAVEDAD) y se aplica siempre, este saltando o no. Eso es lo que
 * permite caminar hasta el borde de una plataforma y caerse, y caer entre
 * plataformas, que era el objetivo pedido.
 *
 * Las dos formas describen la misma parabola; la diferencia es que esta
 * version no necesita saber "cuando empezo el salto" para saber si el
 * personaje debe caer.
 */
public class Jugador extends Actor
{
    /**
     * Estados posibles del jugador (se mantienen los mismos de la guia).
     * Un enum evita numeros magicos y hace explicito que estado maneja
     * cada rama de actualizarAnimacion().
     */
    private enum Estado
    {
        QUIETO,
        CAMINANDO_DERECHA,
        CAMINANDO_IZQUIERDA,
        SALTANDO,
        CAYENDO
    }

    // ------------------------------------------------------------------
    // Ajustes visuales
    // ------------------------------------------------------------------

    /** Los sprites originales miden 16-26 px; x3 los deja legibles en 800x500. */
    private static final int ESCALA = 3;

    /**
     * Todos los sprites se dibujan dentro de un lienzo del MISMO tamano
     * (el del sprite mas grande, jump.png de 26x30) y apoyados al fondo.
     * Sin esto, al cambiar de frame la imagen cambia de alto y el
     * personaje "tiembla" y se hunde en el piso, porque Greenfoot ubica al
     * actor por su centro.
     */
    private static final int ANCHO_IMAGEN = 26 * ESCALA;   // 78
    private static final int ALTO_IMAGEN = 30 * ESCALA;    // 90

    /**
     * Caja de colision: mas angosta que la imagen a proposito. La imagen
     * es ancha por los brazos abiertos del salto; el cuerpo real ocupa
     * bastante menos y no deberia "chocar" con una plataforma que en
     * pantalla ni siquiera esta tocando.
     */
    private static final int ANCHO_CAJA = 14 * ESCALA;     // 42

    /** Cada cuantos act() avanza un frame de la caminata. */
    private static final int CUADROS_POR_FRAME = 6;

    // ------------------------------------------------------------------
    // Ajustes de fisica (probar cambiarlos es parte del ejercicio)
    // ------------------------------------------------------------------

    private static final double GRAVEDAD = 0.8;
    private static final double IMPULSO_SALTO = -14.0;      // negativo = hacia arriba
    private static final double CAIDA_MAXIMA = 16.0;        // velocidad terminal
    private static final int VELOCIDAD_CAMINATA = 5;

    /** Balas simultaneas permitidas (en Mega Man son 3). */
    private static final int MAXIMO_BALAS = 3;

    // ------------------------------------------------------------------
    // Estado interno
    // ------------------------------------------------------------------

    private Estado estado = Estado.QUIETO;

    /** Velocidad vertical actual en px por act(). Negativa = subiendo. */
    private double vy = 0;

    /**
     * Posicion vertical exacta del centro del actor. getY() solo guarda
     * enteros, y redondear en cada act() acumularia error en la fisica.
     */
    private double yReal;

    private boolean enSuelo = false;
    private boolean mirandoIzquierda = false;

    private boolean pidiendoDerecha = false;
    private boolean pidiendoIzquierda = false;

    /** Recuerda si Z ya estaba presionada, para disparar una vez por pulsacion. */
    private boolean teclaDisparoAntes = false;

    private int frameActual = 0;
    private int contadorAnimacion = 0;

    // --- Sprites ya escalados, alineados y espejados ---
    private GreenfootImage quietoDerecha;
    private GreenfootImage quietoIzquierda;
    private GreenfootImage saltoDerecha;
    private GreenfootImage saltoIzquierda;
    private GreenfootImage[] caminarDerecha;
    private GreenfootImage[] caminarIzquierda;

    public Jugador()
    {
        cargarImagenes();
    }

    /** Greenfoot llama a esto al agregar el actor al mundo. */
    protected void addedToWorld(World mundo)
    {
        yReal = getY();
    }

    // ------------------------------------------------------------------
    // Carga de imagenes
    // ------------------------------------------------------------------

    private void cargarImagenes()
    {
        quietoDerecha = prepararSprite("idle.png");
        quietoIzquierda = espejo(quietoDerecha);

        // jump.png se usa tanto para subir como para caer, igual que en el
        // Mega Man original (no existe un sprite de caida distinto).
        saltoDerecha = prepararSprite("jump.png");
        saltoIzquierda = espejo(saltoDerecha);

        caminarDerecha = new GreenfootImage[4];
        caminarDerecha[0] = prepararSprite("walk1.png");
        caminarDerecha[1] = prepararSprite("walk2.png");
        caminarDerecha[2] = prepararSprite("walk3.png");
        caminarDerecha[3] = prepararSprite("walk4.png");

        caminarIzquierda = new GreenfootImage[caminarDerecha.length];

        for (int i = 0; i < caminarDerecha.length; i++)
        {
            caminarIzquierda[i] = espejo(caminarDerecha[i]);
        }

        setImage(quietoDerecha);
    }

    /**
     * Escala el sprite x ESCALA y lo pega, centrado y apoyado al fondo,
     * dentro de un lienzo de tamano fijo (ANCHO_IMAGEN x ALTO_IMAGEN).
     * "Apoyado al fondo" = los pies del personaje quedan siempre en la
     * misma linea, sin importar que frame se muestre.
     */
    private GreenfootImage prepararSprite(String archivo)
    {
        GreenfootImage original = new GreenfootImage(archivo);

        int ancho = original.getWidth() * ESCALA;
        int alto = original.getHeight() * ESCALA;
        original.scale(ancho, alto);

        GreenfootImage lienzo = new GreenfootImage(ANCHO_IMAGEN, ALTO_IMAGEN);
        lienzo.drawImage(original, (ANCHO_IMAGEN - ancho) / 2, ALTO_IMAGEN - alto);

        return lienzo;
    }

    private GreenfootImage espejo(GreenfootImage imagen)
    {
        GreenfootImage copia = new GreenfootImage(imagen);
        copia.mirrorHorizontally();
        return copia;
    }

    // ------------------------------------------------------------------
    // Ciclo principal
    // ------------------------------------------------------------------

    public void act()
    {
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

    /** Mueve en X sin dejar que el personaje se salga del escenario. */
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

    /**
     * El salto solo aplica un impulso inicial hacia arriba. De ahi en
     * adelante la gravedad se encarga del resto, por eso no hace falta
     * llevar un contador de "tiempo de salto".
     */
    private void controlarSalto()
    {
        if (Greenfoot.isKeyDown("space") && enSuelo)
        {
            vy = IMPULSO_SALTO;
            enSuelo = false;
        }
    }

    /**
     * Gravedad continua + aterrizaje.
     *
     * Cada act():
     *   1. la velocidad vertical aumenta (vy += GRAVEDAD);
     *   2. se calcula donde quedarian los pies despues de moverse;
     *   3. si el personaje va bajando y en ese recorrido cruza la
     *      superficie de una Plataforma, se apoya exactamente encima.
     *
     * El paso 3 revisa el TRAMO recorrido (no solo la posicion final)
     * para que a alta velocidad de caida no atraviese una plataforma
     * delgada.
     */
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

    /**
     * Busca la plataforma sobre la que el jugador debe quedar apoyado.
     *
     * Condiciones:
     *  - hay superposicion horizontal entre la caja del jugador y la
     *    plataforma (si no, el personaje esta al lado, no encima);
     *  - la superficie de la plataforma queda dentro del tramo que los
     *    pies recorren en este act().
     *
     * Si hay varias candidatas (plataformas apiladas), gana la mas alta,
     * que es la primera que los pies tocarian al caer.
     */
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

            // El "- 1" da un pixel de tolerancia para que, estando ya
            // apoyado, el jugador siga detectando el suelo cada act().
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

    /** Coordenada Y de la cara superior de una plataforma. */
    private double superficieDe(Plataforma plataforma)
    {
        return plataforma.getY() - plataforma.getImage().getHeight() / 2.0;
    }

    /** Coordenada Y de los pies (borde inferior del lienzo del sprite). */
    private double pies()
    {
        return yReal + ALTO_IMAGEN / 2.0;
    }

    private void ubicarPies(double nuevaAlturaDePies)
    {
        yReal = nuevaAlturaDePies - ALTO_IMAGEN / 2.0;
        setLocation(getX(), (int) Math.round(yReal));
    }

    /**
     * Traduce la situacion fisica del personaje a uno de los estados.
     * En el aire manda el estado aereo; en el suelo, si hay tecla de
     * direccion presionada, camina; si no, queda quieto.
     */
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

    // ------------------------------------------------------------------
    // Disparo
    // ------------------------------------------------------------------

    /**
     * Dispara una vez por pulsacion de Z (no mientras se mantiene
     * apretada) y con un maximo de balas simultaneas en pantalla, igual
     * que el Mega Buster original.
     */
    private void controlarDisparo()
    {
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

        // La bala nace al costado del personaje, a la altura del brazo.
        int x = getX() + direccion * (ANCHO_CAJA / 2 + 4);
        int y = getY() + 5;

        getWorld().addObject(new Bala(direccion), x, y);
    }

    // ------------------------------------------------------------------
    // Animacion
    // ------------------------------------------------------------------

    private void actualizarAnimacion()
    {
        if (estado == Estado.SALTANDO || estado == Estado.CAYENDO)
        {
            setImage(mirandoIzquierda ? saltoIzquierda : saltoDerecha);
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
            setImage(mirandoIzquierda ? quietoIzquierda : quietoDerecha);
            reiniciarCaminata();
        }
    }

    /**
     * Cambia de frame cada CUADROS_POR_FRAME llamadas, no en cada act():
     * a 50 act() por segundo la caminata seria un borron.
     */
    private void animarCaminata(boolean izquierda)
    {
        contadorAnimacion++;

        if (contadorAnimacion >= CUADROS_POR_FRAME)
        {
            frameActual = (frameActual + 1) % caminarDerecha.length;
            contadorAnimacion = 0;
        }

        setImage(izquierda
            ? caminarIzquierda[frameActual]
            : caminarDerecha[frameActual]);
    }

    /** Al detenerse, la proxima caminata empieza desde el primer frame. */
    private void reiniciarCaminata()
    {
        frameActual = 0;
        contadorAnimacion = 0;
    }
}
