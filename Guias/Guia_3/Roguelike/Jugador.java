import greenfoot.*;

public class Jugador extends Soldado
{
    private static final int RADIO = 26;
    private static final int MAX_BALAS = 60;
    private static final int CADENCIA = 8;
    private static final int ACTS_POR_FRAME = 3;

    private int dx = 0;
    private int dy = 0;
    private int dirMovX = 0;
    private int dirMovY = 1;
    private int dirCabeza = FabricaImagenes.ABAJO;
    private int frame = 0;
    private int contadorAnim = 0;
    private int cooldownDisparo = 0;
    private int invulnerable = 0;

    public Jugador()
    {
        super(100, 4);
        setImage(FabricaImagenes.jugador(dirCabeza, FabricaImagenes.cuerpoQuieto()));
    }

    public void act()
    {
        mover();
        apuntarYDisparar();
        animar();
        if (cooldownDisparo > 0) cooldownDisparo--;
        if (invulnerable > 0) invulnerable--;
    }

    private void mover()
    {
        dx = 0;
        dy = 0;

        if (Greenfoot.isKeyDown("w")) dy = -velocidad;
        if (Greenfoot.isKeyDown("s")) dy = velocidad;
        if (Greenfoot.isKeyDown("a")) dx = -velocidad;
        if (Greenfoot.isKeyDown("d")) dx = velocidad;

        if (dx != 0)
        {
            dirMovX = dx > 0 ? 1 : -1;
            dirMovY = 0;
        }
        else if (dy != 0)
        {
            dirMovX = 0;
            dirMovY = dy > 0 ? 1 : -1;
        }

        JuegoWorld mundo = (JuegoWorld) getWorld();
        setLocation(mundo.limitarX(getX() + dx, RADIO),
                    mundo.limitarY(getY() + dy, RADIO));
    }

    /** Flechas: apuntan y disparan. Espacio: dispara hacia donde vas caminando. */
    private void apuntarYDisparar()
    {
        int tiroX = 0;
        int tiroY = 0;

        if (Greenfoot.isKeyDown("left"))       { tiroX = -1; }
        else if (Greenfoot.isKeyDown("right")) { tiroX = 1; }
        else if (Greenfoot.isKeyDown("up"))    { tiroY = -1; }
        else if (Greenfoot.isKeyDown("down"))  { tiroY = 1; }
        else if (Greenfoot.isKeyDown("space")) { tiroX = dirMovX; tiroY = dirMovY; }

        if (tiroX == 0 && tiroY == 0)
        {
            dirCabeza = direccionDe(dirMovX, dirMovY);
            return;
        }

        dirCabeza = direccionDe(tiroX, tiroY);

        if (cooldownDisparo > 0) return;
        if (getWorld().getObjects(Bala.class).size() >= MAX_BALAS) return;

        Bala bala = new Bala(tiroX, tiroY);
        getWorld().addObject(bala, getX() + tiroX * 12, getY() - 10 + tiroY * 8);
        cooldownDisparo = CADENCIA;
    }

    private int direccionDe(int x, int y)
    {
        if (x > 0) return FabricaImagenes.DERECHA;
        if (x < 0) return FabricaImagenes.IZQUIERDA;
        if (y < 0) return FabricaImagenes.ARRIBA;
        return FabricaImagenes.ABAJO;
    }

    private void animar()
    {
        int cuerpo;

        if (dx == 0 && dy == 0)
        {
            frame = 0;
            contadorAnim = 0;
            cuerpo = FabricaImagenes.cuerpoQuieto();
        }
        else
        {
            contadorAnim++;
            if (contadorAnim >= ACTS_POR_FRAME)
            {
                contadorAnim = 0;
                frame = (frame + 1) % 90;
            }
            cuerpo = dx != 0
                   ? FabricaImagenes.cuerpoLateral(frame, dx > 0)
                   : FabricaImagenes.cuerpoVertical(frame);
        }

        setImage(FabricaImagenes.jugador(dirCabeza, cuerpo));
    }

    public void recibirDanio(int cantidad)
    {
        if (invulnerable > 0) return;
        invulnerable = 20;
        super.recibirDanio(cantidad);
    }

    protected void morir()
    {
        World mundo = getWorld();
        if (mundo instanceof JuegoWorld)
        {
            ((JuegoWorld) mundo).gameOver();
        }
        if (mundo != null) mundo.removeObject(this);
    }
}
