import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

public class PacmanWorld extends World {
    private Score score;
    private Pacman pacman;
    private Fantasma blinky;

    public PacmanWorld() {    
        super(28, 36, 16);
        score = new Score("1UP ");
        Greenfoot.setSpeed(35);
        cargarNivel();
    }

    public Pacman getPacman() { return pacman; }
    public Fantasma getBlinky() { return blinky; }

    public void asustarFantasmas() {
        List<Fantasma> fantasmas = getObjects(Fantasma.class);
        for (Fantasma f : fantasmas) {
            f.activarAsustado();
        }
    }

    public void addPunto(int valor) {
        score.addPuntos(valor);
    }

    public void cargarNivel() {
        int[] nivel = {
            // Fila 0 a 2: HUD Superior
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,

            // Fila 3 a 7: Muros superiores y pasillos
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
            1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,
            1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,
            1,6,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,6,1,
            1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,

            // Fila 8: Pasillo transversal superior
            1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,

            // Fila 9 a 12: Bloques centrales
            1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,
            1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,
            1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,
            1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1,

            // Fila 13: Pasillos hacia los túneles laterales
            0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0,

            // Fila 14 a 16: Parte superior de la Casa de Fantasmas
            0,0,0,0,0,1,3,1,1,0,0,0,0,0,8,0,0,0,0,1,1,3,1,0,0,0,0,0,
            0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0,
            1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,

            // Fila 17: Túnel lateral e interior de la Casa de Fantasmas
            0,0,0,0,0,0,3,0,0,0,1,0,9,10,11,0,0,1,0,0,0,3,0,0,0,0,0,0,

            // Fila 18 a 22: Parte inferior de la Casa de Fantasmas y muros laterales
            1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,
            0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,
            0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,
            0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,
            1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,

            // Fila 23: Pasillo transversal inferior
            1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,

            // Fila 24 a 25: Bloques en T inferiores
            1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,
            1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,

            // Fila 26: Pasillo con Power Pellets y Spawn de Pacman
            1,6,3,3,1,1,3,3,3,3,3,3,3,2,0,3,3,3,3,3,3,3,1,1,3,3,6,1,

            // Fila 27 a 28: Obstáculos finales inferiores
            1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,
            1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,

            // Fila 29 a 31: Pasillo inferior y base
            1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,
            1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,
            1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,

            // Fila 32: Pasillo con Fruta
            1,3,3,3,3,3,3,3,3,3,3,3,3,3,7,0,3,3,3,3,3,3,3,3,3,3,3,1,

            // Fila 33: Muro base del mapa
            1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,

            // Fila 34 a 35: Espacio negro inferior
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
        };

        cargarObjetos(nivel);
    }

    private void cargarObjetos(int[] arreglo) {
        int i = 0;
        for (int y = 0; y < 36; y++) {
            for (int x = 0; x < 28; x++) {
                if (i >= arreglo.length) break;

                int tipo = arreglo[i];
                if (tipo == 1) {
                    addObject(new Muro(), x, y);
                } else if (tipo == 2) {
                    pacman = new Pacman();
                    addObject(pacman, x, y);
                } else if (tipo == 3) {
                    addObject(new Bolita(), x, y);
                } else if (tipo == 4) {
                    addObject(score, x, y);
                } else if (tipo == 6) {
                    addObject(new PowerPellet(), x, y);
                } else if (tipo == 7) {
                    addObject(new Fruta(), x, y);
                } else if (tipo == 8) {
                    blinky = new Fantasma(Fantasma.TipoFantasma.BLINKY);
                    addObject(blinky, x, y);
                } else if (tipo == 9) {
                    addObject(new Fantasma(Fantasma.TipoFantasma.PINKY), x, y);
                } else if (tipo == 10) {
                    addObject(new Fantasma(Fantasma.TipoFantasma.INKY), x, y);
                } else if (tipo == 11) {
                    addObject(new Fantasma(Fantasma.TipoFantasma.CLYDE), x, y);
                }
                i++;
            }
        }
    }
}