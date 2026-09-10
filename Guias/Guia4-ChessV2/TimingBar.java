import greenfoot.*;

/**
 * Minijuego de precision. Congela el resto del juego (el mundo pasa a MINIGAME
 * y todos los act() se bloquean) mientras un cursor recorre la barra.
 *
 * MODO ATAQUE  (ataca el jugador): zona roja = critico, amarilla = golpe, gris = fallo.
 * MODO DEFENSA (ataca la IA):      zona verde = parada (dano a la mitad).
 */
public class TimingBar extends Actor
{
    public enum Mode { ATTACK, DEFENSE }

    private static final int WIDTH = 300;
    private static final int HEIGHT = 46;
    private static final int ARM_DELAY = 10;   // ticks antes de aceptar input
    private static final int TIMEOUT = 300;    // ticks antes de resolver solo
    private static final int RESULT_TICKS = 45;

    private Mode mode;
    private Piece attacker;
    private Piece defender;
    private int targetX;
    private int targetY;

    private int cursorX = 0;
    private int speed;
    private boolean movingRight = true;

    private int ticks = 0;
    private boolean resolved = false;
    private int resultCountdown = 0;
    private String resultText = "";
    private Color resultColor = Color.WHITE;
    private int finalDamage = 0;
    private boolean defenderDied = false;

    public TimingBar(Piece attacker, Piece defender, Mode mode, int targetX, int targetY, int level)
    {
        this.attacker = attacker;
        this.defender = defender;
        this.mode = mode;
        this.targetX = targetX;
        this.targetY = targetY;
        this.speed = Math.min(12, 4 + level / 3);   // la dificultad sube con el nivel
        drawBar();
    }

    public void act()
    {
        if (resolved) {
            resultCountdown--;
            if (resultCountdown <= 0) finish();
            return;
        }

        ticks++;
        moveCursor();
        drawBar();

        if (ticks > ARM_DELAY) {
            String key = Greenfoot.getKey();   // FIX: evento unico, no isKeyDown
            if ("space".equals(key)) {
                resolve();
                return;
            }
        }
        if (ticks > TIMEOUT) {
            resolve();   // timeout: nunca se queda colgado
        }
    }

    private void moveCursor()
    {
        if (movingRight) {
            cursorX += speed;
            if (cursorX >= WIDTH - 5) { cursorX = WIDTH - 5; movingRight = false; }
        } else {
            cursorX -= speed;
            if (cursorX <= 0) { cursorX = 0; movingRight = true; }
        }
    }

    // Zonas de la barra
    private boolean inCrit()   { return cursorX >= 135 && cursorX <= 165; }
    private boolean inNormal() { return cursorX >= 75  && cursorX <= 225; }
    private boolean inParry()  { return cursorX >= 128 && cursorX <= 172; }

    private void resolve()
    {
        resolved = true;

        if (mode == Mode.ATTACK) {
            int base = attacker.getAttack();
            if (inCrit()) {
                finalDamage = base * attacker.getCritMultiplier();
                resultText = "CRITICO  -" + finalDamage;
                resultColor = new Color(255, 90, 90);
            } else if (inNormal()) {
                finalDamage = base;
                resultText = "GOLPE  -" + finalDamage;
                resultColor = new Color(255, 220, 90);
            } else {
                finalDamage = 0;
                resultText = "FALLO";
                resultColor = new Color(190, 190, 190);
            }
        } else {
            int base = attacker.getAttack();
            if (inParry()) {
                finalDamage = Math.max(1, base / 2);
                resultText = "PARADA  -" + finalDamage;
                resultColor = new Color(120, 230, 140);
            } else {
                finalDamage = base;
                resultText = "TE GOLPEAN  -" + finalDamage;
                resultColor = new Color(255, 120, 120);
            }
        }

        defenderDied = defender.applyDamage(finalDamage);
        resultCountdown = RESULT_TICKS;

        // Efectos del impacto sobre el tablero: embestida del atacante, temblor
        // y destello del defensor y el numero flotante. El resultado ya no vive
        // solo dentro de esta barra, que tapa media pantalla.
        World w = getWorld();
        if (w instanceof ChessWorld) {
            String fxText = (finalDamage > 0) ? ("-" + finalDamage) : "FALLO";
            if (mode == Mode.ATTACK && inCrit() && finalDamage > 0) fxText = "CRITICO -" + finalDamage;
            ((ChessWorld) w).playHitFx(attacker, defender, targetX, targetY,
                                       finalDamage, fxText, resultColor);
        }

        drawBar();
    }

    private void finish()
    {
        World w = getWorld();
        if (!(w instanceof ChessWorld)) return;
        ChessWorld world = (ChessWorld) w;
        world.removeObject(this);
        world.onCombatResolved(attacker, defender, defenderDied, targetX, targetY, mode);
    }

    private void drawBar()
    {
        GreenfootImage img = new GreenfootImage(WIDTH + 20, HEIGHT + 60);

        img.setColor(new Color(15, 15, 22, 235));
        img.fillRect(0, 0, WIDTH + 20, HEIGHT + 60);
        img.setColor(new Color(215, 180, 90));
        img.drawRect(0, 0, WIDTH + 19, HEIGHT + 59);

        // Titulo (se encoge solo si no cabe)
        String title = (mode == Mode.ATTACK)
            ? attacker.getPieceType().toUpperCase() + " ataca - ESPACIO para golpear"
            : "DEFIENDE - ESPACIO en la zona verde para parar";
        TextUtil.drawFitted(img, title, 10, 4, WIDTH, 14, 9, Color.WHITE);

        int top = 26;

        // Fondo de la barra
        img.setColor(new Color(70, 70, 80));
        img.fillRect(10, top, WIDTH, HEIGHT);

        if (mode == Mode.ATTACK) {
            img.setColor(new Color(210, 180, 50));
            img.fillRect(10 + 75, top, 150, HEIGHT);
            img.setColor(new Color(200, 45, 45));
            img.fillRect(10 + 135, top, 30, HEIGHT);
        } else {
            img.setColor(new Color(50, 170, 80));
            img.fillRect(10 + 128, top, 44, HEIGHT);
        }

        // Cursor
        img.setColor(Color.WHITE);
        img.fillRect(10 + cursorX, top - 4, 5, HEIGHT + 8);

        img.setColor(new Color(215, 180, 90));
        img.drawRect(10, top, WIDTH - 1, HEIGHT - 1);

        // Resultado
        if (resolved) {
            TextUtil.drawFitted(img, resultText, 10, HEIGHT + 34, WIDTH, 18, 10, resultColor);
        } else {
            TextUtil.drawFitted(img, defender.getPieceType().toUpperCase() + "  HP "
                                + defender.getHp() + "/" + defender.getMaxHp(),
                                10, HEIGHT + 36, WIDTH, 13, 9, new Color(190, 190, 200));
        }

        setImage(img);
    }
}
