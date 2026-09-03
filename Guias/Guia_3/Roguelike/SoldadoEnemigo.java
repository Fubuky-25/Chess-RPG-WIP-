import greenfoot.*;

public class SoldadoEnemigo extends Soldado
{
    private static final int RADIO = 24;
    private static final int ACTS_POR_FRAME = 12;
    private static final int ESPERA_ATAQUE = 30;

    private Jugador objetivo;
    private int danio;
    private int cooldownAtaque = 0;
    private int contadorAnim = 0;
    private int frame = 0;
    private int destello = 0;

    public SoldadoEnemigo(Jugador objetivo, int vida, int velocidad, int danio)
    {
        super(vida, velocidad);
        this.objetivo = objetivo;
        this.danio = danio;
        setImage(FabricaImagenes.enemigo(0));
    }

    public void act()
    {
        if (objetivo == null || objetivo.getWorld() == null) return;

        perseguir();
        animar();
        if (cooldownAtaque > 0) cooldownAtaque--;
        atacar();
    }

    private void perseguir()
    {
        int dx = objetivo.getX() - getX();
        int dy = objetivo.getY() - getY();
        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia > 0)
        {
            int movX = (int) Math.round(velocidad * dx / distancia);
            int movY = (int) Math.round(velocidad * dy / distancia);
            JuegoWorld mundo = (JuegoWorld) getWorld();
            setLocation(mundo.limitarX(getX() + movX, RADIO),
                        mundo.limitarY(getY() + movY, RADIO));
        }
    }

    private void animar()
    {
        contadorAnim++;
        if (contadorAnim >= ACTS_POR_FRAME)
        {
            contadorAnim = 0;
            frame = (frame + 1) % 2;
        }

        if (destello > 0)
        {
            destello--;
            setImage(FabricaImagenes.enemigoGolpeado(frame));
        }
        else
        {
            setImage(FabricaImagenes.enemigo(frame));
        }
    }

    private void atacar()
    {
        if (isTouching(Jugador.class) && cooldownAtaque == 0)
        {
            objetivo.recibirDanio(danio);
            cooldownAtaque = ESPERA_ATAQUE;
        }
    }

    public void recibirDanio(int cantidad)
    {
        destello = 4;
        super.recibirDanio(cantidad);
    }
}
