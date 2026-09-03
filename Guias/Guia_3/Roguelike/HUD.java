import greenfoot.*;

public class HUD extends Actor
{
    private static final int CADA_CUANTO = 10;
    private static final int ANCHO_BARRA = 120;

    private JuegoWorld mundo;
    private Jugador jugador;
    private GestorOleadas gestor;
    private int contador = 0;

    public HUD(JuegoWorld mundo, Jugador jugador, GestorOleadas gestor)
    {
        this.mundo = mundo;
        this.jugador = jugador;
        this.gestor = gestor;
        actualizarImagen();
    }

    public void act()
    {
        contador++;
        if (contador >= CADA_CUANTO)
        {
            actualizarImagen();
            contador = 0;
        }
    }

    private void actualizarImagen()
    {
        int enemigos = mundo.getObjects(SoldadoEnemigo.class).size();
        int balas = mundo.getObjects(Bala.class).size();
        int vida = Math.max(0, jugador.getVida());

        String texto = "Oleada: " + gestor.getOleada()
                     + "   Enemigos: " + enemigos
                     + "   Balas: " + balas;

        GreenfootImage letras = new GreenfootImage(texto, 20, Color.WHITE, new Color(0, 0, 0, 140));
        GreenfootImage img = new GreenfootImage(Math.max(letras.getWidth(), ANCHO_BARRA) + 8,
                                                letras.getHeight() + 16);

        img.setColor(new Color(0, 0, 0, 140));
        img.fillRect(0, 0, img.getWidth(), img.getHeight());
        img.drawImage(letras, 4, 2);

        int lleno = ANCHO_BARRA * vida / jugador.getVidaMaxima();
        img.setColor(new Color(70, 20, 20));
        img.fillRect(4, letras.getHeight() + 4, ANCHO_BARRA, 8);
        img.setColor(new Color(200, 40, 50));
        img.fillRect(4, letras.getHeight() + 4, lleno, 8);

        setImage(img);
    }
}
