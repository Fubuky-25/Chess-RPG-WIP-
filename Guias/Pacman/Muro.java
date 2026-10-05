import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Muro extends Actor {
    public Muro() {
        GreenfootImage img = new GreenfootImage(16, 16);
        img.setColor(new Color(33, 33, 222)); // Azul arcade
        img.fillRect(0, 0, 16, 16);
        setImage(img);
    }
}