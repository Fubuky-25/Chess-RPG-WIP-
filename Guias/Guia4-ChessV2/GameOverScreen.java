import greenfoot.*;

/** Pantalla final. Con el estado en GAME_OVER todo lo demas queda congelado. */
public class GameOverScreen extends Actor
{
    public GameOverScreen(int level, int piecesLost)
    {
        int W = 440, H = 190;
        GreenfootImage img = new GreenfootImage(W, H);
        img.setColor(new Color(10, 10, 16, 245));
        img.fillRect(0, 0, W, H);
        img.setColor(new Color(200, 60, 60));
        img.drawRect(0, 0, W - 1, H - 1);
        img.drawRect(4, 4, W - 9, H - 9);

        TextUtil.drawFitted(img, "GAME OVER", 20, 22, W - 40, 40, 20, new Color(230, 70, 70));
        TextUtil.drawFitted(img, "Tu Rey ha caido.", 24, 82, W - 48, 17, 10, Color.WHITE);
        TextUtil.drawFitted(img, "Nivel alcanzado: " + level, 24, 110, W - 48, 17, 10, Color.WHITE);
        TextUtil.drawFitted(img, "Piezas perdidas: " + piecesLost, 24, 138, W - 48, 17, 10, Color.WHITE);

        setImage(img);
    }
}
