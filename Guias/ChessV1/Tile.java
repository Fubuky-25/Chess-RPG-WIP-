import greenfoot.*;

public class Tile extends Actor {
    private int fixedX;
    private int fixedY;

    public Tile(String type, int x, int y) {
        this.fixedX = x;
        this.fixedY = y;
        
        // Carga exactamente las imágenes White.png, Black.png y background.png
        if (type.equalsIgnoreCase("White")) {
            setImage("White.png");
        } else if (type.equalsIgnoreCase("Black")) {
            setImage("Black.png");
        } else if (type.equalsIgnoreCase("background")) {
            setImage("background.png");
        }
    }

    public void act() {
        // Bloquea el arrastre manteniendo las casillas estáticas
        if (getX() != fixedX || getY() != fixedY) {
            setLocation(fixedX, fixedY);
        }
    }

    public int getBoardX() { return fixedX; }
    public int getBoardY() { return fixedY; }
}