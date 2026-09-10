import greenfoot.*;

public class StartButton extends Actor {
    public StartButton() {
        GreenfootImage img = new GreenfootImage(160, 40);
        img.setColor(Color.GREEN);
        img.fillRect(0, 0, 160, 40);
        img.setColor(Color.BLACK);
        img.drawRect(0, 0, 159, 39);
        img.setFont(new Font("Arial", true, false, 14));
        img.drawString("EMPEZAR JUEGO", 15, 25);
        setImage(img);
    }

    public void act() {
        if (Greenfoot.mouseClicked(this)) {
            ChessWorld world = (ChessWorld) getWorld();
            world.startGameFromButton();
        }
    }
}