import greenfoot.*;

/**
 * Decide cuantos enemigos aparecen y que tan duros son.
 * El enemigo no sabe en que oleada esta: eso vive aca.
 */
public class GestorOleadas
{
    private static final int TOPE_SIMULTANEOS = 35;
    private static final int PAUSA_ENTRE_OLEADAS = 100;

    private JuegoWorld mundo;
    private Jugador jugador;
    private int oleada = 1;
    private int variante = 0;
    private int generados = 0;
    private int objetivoOleada = 0;
    private int contadorSpawn = 0;
    private int esperaEntreOleadas = 0;

    public GestorOleadas(JuegoWorld mundo, Jugador jugador)
    {
        this.mundo = mundo;
        this.jugador = jugador;
        prepararOleada();
    }

    public void actualizar()
    {
        if (jugador.getWorld() == null) return;

        int activos = mundo.getObjects(SoldadoEnemigo.class).size();

        if (generados < objetivoOleada)
        {
            contadorSpawn--;
            if (contadorSpawn <= 0 && activos < maxSimultaneos())
            {
                crearEnemigo();
                generados++;
                contadorSpawn = intervaloSpawn();
            }
        }
        else if (activos == 0)
        {
            if (esperaEntreOleadas == 0)
            {
                esperaEntreOleadas = PAUSA_ENTRE_OLEADAS;
                mundo.showText("Oleada " + (oleada + 1), mundo.getWidth() / 2, mundo.getHeight() / 2);
            }

            esperaEntreOleadas--;
            if (esperaEntreOleadas <= 0)
            {
                mundo.showText(null, mundo.getWidth() / 2, mundo.getHeight() / 2);
                oleada++;
                prepararOleada();
            }
        }
    }

    private void prepararOleada()
    {
        generados = 0;
        contadorSpawn = 20;
        esperaEntreOleadas = 0;
        variante = oleada == 1 ? 0 : Greenfoot.getRandomNumber(4);
        objetivoOleada = Math.min(5 + oleada * 3, 80);

        if (variante == 1) objetivoOleada = objetivoOleada * 13 / 10;   // muchos y debiles
        if (variante == 2) objetivoOleada = objetivoOleada * 7 / 10;    // pocos y duros
    }

    private int maxSimultaneos()
    {
        return Math.min(6 + oleada * 2, TOPE_SIMULTANEOS);
    }

    private int intervaloSpawn()
    {
        int intervalo = Math.max(12, 50 - oleada * 3);
        if (variante == 3) intervalo = intervalo * 6 / 10;              // aparicion mas rapida
        return Math.max(8, intervalo);
    }

    private int vidaEnemigo()
    {
        int vida = 20 + oleada * 4;
        if (variante == 1) vida = vida * 3 / 4;
        if (variante == 2) vida = vida * 7 / 5;
        return vida;
    }

    private int velocidadEnemigo()
    {
        return Math.min(1 + oleada / 4, 4);
    }

    private int danioEnemigo()
    {
        return Math.min(4 + oleada, 15);
    }

    private void crearEnemigo()
    {
        SoldadoEnemigo enemigo = new SoldadoEnemigo(
            jugador,
            vidaEnemigo(),
            velocidadEnemigo(),
            danioEnemigo());

        int izq = JuegoWorld.MURO_LATERAL + 30;
        int der = mundo.getWidth() - JuegoWorld.MURO_LATERAL - 30;
        int arr = JuegoWorld.MURO_SUPERIOR + 30;
        int aba = mundo.getHeight() - JuegoWorld.MURO_INFERIOR - 30;

        int lado = Greenfoot.getRandomNumber(4);
        int x;
        int y;

        if (lado == 0)      { x = izq; y = arr + Greenfoot.getRandomNumber(aba - arr); }
        else if (lado == 1) { x = der; y = arr + Greenfoot.getRandomNumber(aba - arr); }
        else if (lado == 2) { x = izq + Greenfoot.getRandomNumber(der - izq); y = arr; }
        else                { x = izq + Greenfoot.getRandomNumber(der - izq); y = aba; }

        mundo.addObject(enemigo, x, y);
    }

    public int getOleada()
    {
        return oleada;
    }
}
