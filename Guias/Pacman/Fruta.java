import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Fruta extends Actor {
    public Fruta() {
        try {
            // Cargar sprite y forzar el resize a 16x16
            GreenfootImage img = new GreenfootImage("cereza.png");
            img.scale(16, 16);
            setImage(img);
        } catch (IllegalArgumentException e) {
            // Si no encuentra cereza.png, dibuja una fruta básica
            GreenfootImage img = new GreenfootImage(16, 16);
            img.setColor(Color.RED);
            img.fillOval(2, 4, 8, 8);
            img.setColor(Color.GREEN);
            img.fillRect(6, 1, 2, 4);
            setImage(img);
        }
    }
}