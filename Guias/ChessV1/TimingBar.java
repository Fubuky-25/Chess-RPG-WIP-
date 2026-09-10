import greenfoot.*;

public class TimingBar extends Actor {
    private int cursorX = 0;
    private int speed = 5;
    private boolean movingRight = true;
    private final int width = 200;
    private final int height = 30;
    
    private Piece attacker;
    private Piece defender;

    public TimingBar(Piece attacker, Piece defender) {
        this.attacker = attacker;
        this.defender = defender;
        drawBar();
    }

    public void act() {
        moveCursor();
        drawBar();
        checkInput();
    }

    private void moveCursor() {
        if (movingRight) {
            cursorX += speed;
            if (cursorX >= width) movingRight = false;
        } else {
            cursorX -= speed;
            if (cursorX <= 0) movingRight = true;
        }
    }

    private void drawBar() {
        GreenfootImage img = new GreenfootImage(width, height);
        
        img.setColor(Color.GRAY);
        img.fillRect(0, 0, width, height);

        img.setColor(Color.YELLOW);
        img.fillRect(50, 0, 100, height);

        img.setColor(Color.RED);
        img.fillRect(85, 0, 30, height);

        img.setColor(Color.WHITE);
        img.fillRect(cursorX, 0, 4, height);

        setImage(img);
    }

    private void checkInput() {
        if (Greenfoot.isKeyDown("space")) {
            int damage = attacker.getAttack();
            
            if (cursorX >= 85 && cursorX <= 115) {
                damage *= 2;
                getWorld().showText("¡CRÍTICO!", getX(), getY() - 30);
            } else if (cursorX >= 50 && cursorX <= 150) {
                getWorld().showText("¡GOLPE!", getX(), getY() - 30);
            } else {
                damage = 0;
                getWorld().showText("¡FALLO!", getX(), getY() - 30);
            }

            defender.takeDamage(damage);
            
            ChessWorld world = (ChessWorld) getWorld();
            world.endMinigame();
            world.removeObject(this);
        }
    }
}