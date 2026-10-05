import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Fantasma extends Actor {
    public enum TipoFantasma { BLINKY, PINKY, INKY, CLYDE }
    private TipoFantasma tipo;

    private int dirX = 0;
    private int dirY = -1; // Comienzan saliendo hacia arriba
    private boolean asustado = false;
    private int tiempoAsustado = 0;

    // Arrays para los sprites escalados a 16x16
    private GreenfootImage[] imgsNormales = new GreenfootImage[4];
    private GreenfootImage[] imgsAsustado = new GreenfootImage[2];
    
    // Variables para la animación
    private int frameAnim = 0;
    private int contadorAnim = 0;

    public Fantasma(TipoFantasma tipo) {
        this.tipo = tipo;
        cargarSprites();
    }

    private void cargarSprites() {
        String baseName = "";
        switch (tipo) {
            case BLINKY: baseName = "blinky"; break;
            case PINKY:  baseName = "pinky"; break;
            case INKY:   baseName = "inky"; break;
            case CLYDE:  baseName = "clyde"; break;
        }

        // Cargar los 4 frames direccionales del 0 al 3
        for (int i = 0; i < 4; i++) {
            try {
                GreenfootImage img = new GreenfootImage(baseName + "-" + i + ".png");
                img.scale(16, 16); // Escalar al tamaño correcto
                imgsNormales[i] = img;
            } catch (IllegalArgumentException e) {
                // Generar un círculo de color si falta la imagen
                GreenfootImage img = new GreenfootImage(16, 16);
                if (tipo == TipoFantasma.BLINKY) img.setColor(Color.RED);
                else if (tipo == TipoFantasma.PINKY) img.setColor(Color.PINK);
                else if (tipo == TipoFantasma.INKY) img.setColor(Color.CYAN);
                else img.setColor(Color.ORANGE);
                img.fillOval(1, 1, 14, 14);
                imgsNormales[i] = img;
            }
        }

        // Cargar los 2 frames de asustado
        for (int i = 0; i < 2; i++) {
            try {
                GreenfootImage img = new GreenfootImage("spooked-" + i + ".png");
                img.scale(16, 16); // Escalar al tamaño correcto
                imgsAsustado[i] = img;
            } catch (IllegalArgumentException e) {
                GreenfootImage img = new GreenfootImage(16, 16);
                img.setColor(Color.BLUE);
                img.fillOval(1, 1, 14, 14);
                imgsAsustado[i] = img;
            }
        }

        // Imagen inicial mirando hacia arriba
        setImage(imgsNormales[2]); 
    }

    public void act() {
        if (asustado) {
            tiempoAsustado--;
            if (tiempoAsustado <= 0) {
                asustado = false;
            }
        }
        
        decidirMovimiento();
        comprobarTunel();
        comprobarColision();
        animar();
    }

    private void animar() {
        if (asustado) {
            // Animación cíclica alternando 0 y 1 para asustado
            contadorAnim++;
            if (contadorAnim % 5 == 0) {
                frameAnim = (frameAnim + 1) % 2;
                setImage(imgsAsustado[frameAnim]);
            }
        } else {
            // Cambiar la imagen directamente basándose en la dirección
            int index = 0; // Por defecto: Abajo
            
            if (dirX == -1) {
                index = 1; // Izquierda
            } else if (dirY == -1) {
                index = 2; // Arriba
            } else if (dirX == 1) {
                index = 3; // Derecha
            } else if (dirY == 1) {
                index = 0; // Abajo
            }
            
            setImage(imgsNormales[index]);
        }
    }

    public void activarAsustado() {
        this.asustado = true;
        this.tiempoAsustado = 300; // Tiempo que duran asustados
        frameAnim = 0;
        setImage(imgsAsustado[0]);
        // Se dan la vuelta al asustarse (comportamiento clásico)
        dirX = -dirX;
        dirY = -dirY;
    }

    private void decidirMovimiento() {
        int targetX = getX();
        int targetY = getY();

        PacmanWorld mundo = (PacmanWorld) getWorld();
        Pacman pacman = mundo.getPacman();

        if (pacman != null) {
            if (asustado) {
                targetX = Greenfoot.getRandomNumber(mundo.getWidth());
                targetY = Greenfoot.getRandomNumber(mundo.getHeight());
            } else {
                switch (tipo) {
                    case BLINKY: targetX = pacman.getX(); targetY = pacman.getY(); break;
                    case PINKY:  
                        targetX = pacman.getX() + (pacman.getRotation() == 0 ? 4 : pacman.getRotation() == 180 ? -4 : 0);
                        targetY = pacman.getY() + (pacman.getRotation() == 90 ? 4 : pacman.getRotation() == 270 ? -4 : 0);
                        break;
                    case INKY:   
                        targetX = pacman.getX() * 2 - (mundo.getBlinky() != null ? mundo.getBlinky().getX() : 0);
                        targetY = pacman.getY() * 2 - (mundo.getBlinky() != null ? mundo.getBlinky().getY() : 0);
                        break;
                    case CLYDE:  
                        double dist = Math.hypot(getX() - pacman.getX(), getY() - pacman.getY());
                        if (dist > 8) { targetX = pacman.getX(); targetY = pacman.getY(); } 
                        else { targetX = 0; targetY = mundo.getHeight(); }
                        break;
                }
            }
        }

        int[][] dirs = {{0, -1}, {-1, 0}, {0, 1}, {1, 0}}; // Arriba, Izquierda, Abajo, Derecha
        double mejorDist = Double.MAX_VALUE;
        int mejorDX = 0;
        int mejorDY = 0;
        boolean movimientoValido = false;

        for (int[] d : dirs) {
            int nx = d[0];
            int ny = d[1];

            // Evitar que el fantasma se dé la vuelta 180 grados bruscamente
            if (nx == -dirX && ny == -dirY) continue;

            // VERIFICACIÓN DOBLE: No chocar con un Muro Y no chocar con otro Fantasma
            if (getOneObjectAtOffset(nx, ny, Muro.class) == null && getOneObjectAtOffset(nx, ny, Fantasma.class) == null) {
                double dist = Math.hypot((getX() + nx) - targetX, (getY() + ny) - targetY);
                if (dist < mejorDist) {
                    mejorDist = dist;
                    mejorDX = nx;
                    mejorDY = ny;
                    movimientoValido = true;
                }
            }
        }

        // Si se quedó encerrado (bloqueado por muros o por otros fantasmas haciendo cola)
        if (!movimientoValido) {
            mejorDX = 0;
            mejorDY = 0; // Se detiene temporalmente
            
            // Intenta cualquier dirección que esté libre (incluso darse la vuelta) para desatascarse
            for (int[] d : dirs) {
                if (getOneObjectAtOffset(d[0], d[1], Muro.class) == null && getOneObjectAtOffset(d[0], d[1], Fantasma.class) == null) {
                    mejorDX = d[0]; 
                    mejorDY = d[1]; 
                    break;
                }
            }
        }

        dirX = mejorDX;
        dirY = mejorDY;
        setLocation(getX() + dirX, getY() + dirY);
    }

    private void comprobarTunel() {
        if (getX() <= 0 && dirX == -1) {
            setLocation(getWorld().getWidth() - 1, getY());
        } else if (getX() >= getWorld().getWidth() - 1 && dirX == 1) {
            setLocation(0, getY());
        }
    }

    private void comprobarColision() {
        Pacman pacman = (Pacman) getOneIntersectingObject(Pacman.class);
        if (pacman != null) {
            if (asustado) {
                PacmanWorld mundo = (PacmanWorld) getWorld();
                mundo.addPunto(200);
                setLocation(13, 17); // Reaparece en el centro (Casa de Fantasmas)
                asustado = false;
                setImage(imgsNormales[0]);
            } else {
                getWorld().removeObject(pacman);
                Greenfoot.playSound("gameover.wav");
                Greenfoot.stop();
            }
        }
    }
}