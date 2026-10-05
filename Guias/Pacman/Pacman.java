import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;

public class Pacman extends Actor {
    private int dirX = -1;
    private int dirY = 0;
    private int nextDirX = -1;
    private int nextDirY = 0;
    
    // Variables para saber si se movió
    private int lastX;
    private int lastY;

    // Animación del sprite
    private ArrayList<GreenfootImage> imagenes;
    private int contadorAnim = 0;
    private int frameAnim = 0;

    public Pacman() {
        imagenes = new ArrayList<GreenfootImage>();
        
        // Cargar y escalar a 16x16
        GreenfootImage img0 = new GreenfootImage("pacman-0.png");
        img0.scale(16, 16);
        imagenes.add(img0);

        GreenfootImage img1 = new GreenfootImage("pacman-1.png");
        img1.scale(16, 16);
        imagenes.add(img1);

        GreenfootImage img2 = new GreenfootImage("pacman-2.png");
        img2.scale(16, 16);
        imagenes.add(img2);

        setImage(imagenes.get(0));
    }

    public void act() {
        lastX = getX();
        lastY = getY();
        
        leerTeclado();
        intentarMover();
        comerObjetos();
        comprobarTunel();
        
        // Animamos solo después de saber si cambió de posición
        animar();
    }

    private void leerTeclado() {
        if (Greenfoot.isKeyDown("left") || Greenfoot.isKeyDown("a"))      { nextDirX = -1; nextDirY = 0; }
        else if (Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d")) { nextDirX = 1;  nextDirY = 0; }
        else if (Greenfoot.isKeyDown("up") || Greenfoot.isKeyDown("w"))    { nextDirX = 0;  nextDirY = -1; }
        else if (Greenfoot.isKeyDown("down") || Greenfoot.isKeyDown("s"))  { nextDirX = 0;  nextDirY = 1; }
    }

    private void intentarMover() {
        if (puedeMoverse(nextDirX, nextDirY)) {
            dirX = nextDirX;
            dirY = nextDirY;
            
            if (dirX == 1) setRotation(0);
            else if (dirX == -1) setRotation(180);
            else if (dirY == -1) setRotation(270);
            else if (dirY == 1) setRotation(90);
        }

        if (puedeMoverse(dirX, dirY)) {
            setLocation(getX() + dirX, getY() + dirY);
        }
    }

    private void animar() {
        // Si la posición cambió (está caminando/comiendo), animamos la boca
        if (getX() != lastX || getY() != lastY) {
            contadorAnim++;
            if (contadorAnim % 4 == 0) {
                int[] ordenFrames = {0, 1, 2, 1};
                frameAnim = (frameAnim + 1) % ordenFrames.length;
                setImage(imagenes.get(ordenFrames[frameAnim]));
            }
        } else {
            // Si está quieto (chocando con un muro), se queda con la boca cerrada
            setImage(imagenes.get(0));
            frameAnim = 0;
        }
    }

    private boolean puedeMoverse(int dx, int dy) {
        return getOneObjectAtOffset(dx, dy, Muro.class) == null;
    }

    private void comerObjetos() {
        PacmanWorld mundo = (PacmanWorld) getWorld();

        Actor bolita = getOneIntersectingObject(Bolita.class);
        if (bolita != null) {
            mundo.removeObject(bolita);
            mundo.addPunto(10);
        }

        Actor pellet = getOneIntersectingObject(PowerPellet.class);
        if (pellet != null) {
            mundo.removeObject(pellet);
            mundo.addPunto(50);
            mundo.asustarFantasmas();
        }

        Actor fruta = getOneIntersectingObject(Fruta.class);
        if (fruta != null) {
            mundo.removeObject(fruta);
            mundo.addPunto(100);
        }
    }

    private void comprobarTunel() {
        if (getX() <= 0 && dirX == -1) {
            setLocation(getWorld().getWidth() - 1, getY());
        } else if (getX() >= getWorld().getWidth() - 1 && dirX == 1) {
            setLocation(0, getY());
        }
    }
}