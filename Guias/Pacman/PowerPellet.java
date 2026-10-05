import greenfoot.*;

public class PowerPellet extends Actor {
    private int timer = 0;

    public PowerPellet() {
        dibujarPellet();
    }

    private void dibujarPellet() {
        GreenfootImage img = new GreenfootImage(12, 12);
        img.setColor(Color.WHITE);
        img.fillOval(0, 0, 12, 12);
        setImage(img);
    }

    public void act() {
        // Efecto parpadeo
        timer++;
        if (timer % 15 == 0) {
            getImage().setTransparency(getImage().getTransparency() == 0 ? 255 : 0);
        }
    }
}