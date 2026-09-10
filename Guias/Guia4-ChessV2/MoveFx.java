import greenfoot.*;

/**
 * Animacion de una pieza que se desplaza: deslizamiento de casilla a casilla
 * (KIND_SLIDE) o embestida hacia el objetivo y vuelta (KIND_LUNGE).
 *
 * POR QUE UN ACTOR APARTE Y NO MOVER LA PIEZA:
 *   El mundo es una rejilla de CELL_SIZE px, asi que setLocation() solo puede
 *   dejar la pieza en el centro de una celda: no existe "medio paso". Ademas
 *   toda la logica del juego (casilla ocupada, camino libre) pregunta por
 *   getObjectsAt(x, y, Piece.class), asi que una pieza a medio camino
 *   contaminaria esas consultas.
 *
 *   La solucion es un actor de decoracion que NO es Piece: se le da una imagen
 *   del doble de una celda y dentro de ella se dibuja el sprite con un desfase
 *   de pixeles. Mientras dura la animacion la pieza real se queda quieta y
 *   oculta en su casilla de origen; al terminar, el mundo la coloca de golpe en
 *   el destino. Las consultas de logica nunca ven un estado intermedio.
 *
 * El actor se anima solo en su act() y avisa al mundo al terminar. Cuando el
 * movimiento consume turno, el mundo pasa a GameState.ANIMATING para que ni el
 * jugador ni la IA actuen mientras tanto.
 */
public class MoveFx extends Actor
{
    /** Que hacer cuando termine la animacion. */
    public static final int AFTER_NONE   = 0;   // solo decorativo (embestida)
    public static final int AFTER_PLAYER = 1;   // el jugador consumio su accion
    public static final int AFTER_ENEMY  = 2;   // la IA consumio su accion

    private static final int KIND_SLIDE = 0;
    private static final int KIND_LUNGE = 1;

    /** Lienzo de 2 celdas: permite desfases de hasta media celda en cada eje. */
    private static final int CANVAS = ChessWorld.CELL_SIZE * 2;

    private static final int LUNGE_TICKS = 18;
    private static final double LUNGE_REACH = 0.42;   // fraccion de celda que avanza

    private Piece piece;
    private GreenfootImage sprite;

    private int kind;
    private int fromPx, fromPy, toPx, toPy;
    private int destX, destY;
    private int tick = 0;
    private int duration;
    private int after;

    /** Ultimo desfase dibujado dentro del lienzo. Debe caber en +-media celda. */
    private int offX = 0, offY = 0;

    /** Deslizamiento de (fromX,fromY) a (toX,toY). */
    public MoveFx(Piece piece, int fromX, int fromY, int toX, int toY, int after)
    {
        this.piece = piece;
        this.kind = KIND_SLIDE;
        this.after = after;
        this.destX = toX;
        this.destY = toY;

        this.fromPx = centerOf(fromX);
        this.fromPy = centerOf(fromY);
        this.toPx   = centerOf(toX);
        this.toPy   = centerOf(toY);

        int cells = Math.max(Math.abs(toX - fromX), Math.abs(toY - fromY));
        this.duration = clamp(7 + 4 * cells, 10, 32);

        snapshot(piece);
        draw(0, 0);
    }

    /** Embestida: avanza hacia (towardX,towardY) y vuelve. No consume turno. */
    public MoveFx(Piece piece, int towardX, int towardY)
    {
        this.piece = piece;
        this.kind = KIND_LUNGE;
        this.after = AFTER_NONE;
        this.destX = piece.getX();
        this.destY = piece.getY();

        this.fromPx = centerOf(piece.getX());
        this.fromPy = centerOf(piece.getY());
        this.toPx   = centerOf(towardX);
        this.toPy   = centerOf(towardY);
        this.duration = LUNGE_TICKS;

        snapshot(piece);
        draw(0, 0);
    }

    /**
     * Copia la imagen actual de la pieza (sprite + barra de HP). Se hace en el
     * constructor, ANTES de que el mundo oculte la pieza real.
     */
    private void snapshot(Piece p)
    {
        GreenfootImage current = p.getImage();
        sprite = (current == null) ? new GreenfootImage(ChessWorld.CELL_SIZE, ChessWorld.CELL_SIZE)
                                   : new GreenfootImage(current);
    }

    public void act()
    {
        World w = getWorld();
        if (!(w instanceof ChessWorld)) return;

        tick++;
        double t = (double) tick / (double) duration;
        if (t > 1.0) t = 1.0;

        double factor;
        if (kind == KIND_SLIDE) {
            factor = ease(t);
        } else {
            // Ida y vuelta: sube hasta 1 en la mitad y baja hasta 0 al final.
            double half = (t <= 0.5) ? (t / 0.5) : ((1.0 - t) / 0.5);
            factor = ease(half) * LUNGE_REACH;
        }

        int px = (int) Math.round(fromPx + (toPx - fromPx) * factor);
        int py = (int) Math.round(fromPy + (toPy - fromPy) * factor);
        place(px, py);

        if (tick >= duration) {
            ((ChessWorld) w).onMoveFxFinished(this);
        }
    }

    /**
     * Coloca el actor en la celda que contiene el punto (px,py) y dibuja el
     * sprite desplazado el resto de pixeles dentro de su propio lienzo.
     */
    private void place(int px, int py)
    {
        int cell = ChessWorld.CELL_SIZE;

        int cx = clamp(px / cell, 0, ChessWorld.WORLD_WIDTH - 1);
        int cy = clamp(py / cell, 0, ChessWorld.WORLD_HEIGHT - 1);
        setLocation(cx, cy);

        offX = px - centerOf(cx);
        offY = py - centerOf(cy);
        draw(offX, offY);
    }

    private void draw(int offX, int offY)
    {
        GreenfootImage img = new GreenfootImage(CANVAS, CANVAS);
        int base = (CANVAS - ChessWorld.CELL_SIZE) / 2;
        img.drawImage(sprite, base + offX, base + offY);
        setImage(img);
    }

    /** Suavizado: arranca y frena despacio (ease-in-out). */
    private static double ease(double t)
    {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        return t * t * (3.0 - 2.0 * t);
    }

    private static int centerOf(int cell)
    {
        return cell * ChessWorld.CELL_SIZE + ChessWorld.CELL_SIZE / 2;
    }

    private static int clamp(int v, int lo, int hi)
    {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    public Piece getPiece()   { return piece; }
    public boolean isSlide()  { return kind == KIND_SLIDE; }
    public int getAfter()     { return after; }
    public int getDestX()     { return destX; }
    public int getDestY()     { return destY; }
    public int getDuration()  { return duration; }
    public int getOffsetX()   { return offX; }
    public int getOffsetY()   { return offY; }
    public static int canvasSize() { return CANVAS; }
}
