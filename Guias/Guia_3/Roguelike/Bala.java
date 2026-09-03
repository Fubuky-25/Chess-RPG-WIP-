import greenfoot.*;

public class Bala extends Actor
{
    private static final int VELOCIDAD = 9;
    private static final int VIDA_UTIL = 80;

    private int dx;
    private int dy;
    private int vidaUtil = VIDA_UTIL;

    public Bala(int dirX, int dirY)
    {
        this.dx = dirX;
        this.dy = dirY;
        setImage(FabricaImagenes.bala());
    }

    public void act()
    {
        setLocation(getX() + dx * VELOCIDAD, getY() + dy * VELOCIDAD);
        vidaUtil--;

        SoldadoEnemigo enemigo = (SoldadoEnemigo) getOneIntersectingObject(SoldadoEnemigo.class);
        if (enemigo != null)
        {
            enemigo.recibirDanio(10);
            getWorld().removeObject(this);
            return;
        }

        if (vidaUtil <= 0 || choco())
        {
            getWorld().removeObject(this);
        }
    }

    private boolean choco()
    {
        JuegoWorld mundo = (JuegoWorld) getWorld();
        return !mundo.dentroDeLaSala(getX(), getY());
    }
}
