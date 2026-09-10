import greenfoot.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mundo del juego: orquesta la maquina de estados y delega el trabajo pesado en
 * TurnManager, EnemyAI, LevelGenerator y RewardScreen.
 *
 * Rejilla 12x11 de 64 px:
 *   filas 0-1   -> HUD y botones
 *   x 2..9 / y 2..9 -> tablero de ajedrez 8x8
 *      y 2..3  -> zona de aparicion enemiga (procedural)
 *      y 8..9  -> zona de despliegue del jugador
 *   fila 10     -> banca de piezas sin desplegar
 *   columnas 0-1 -> panel de inventario
 */
public class ChessWorld extends World
{
    public static final int WORLD_WIDTH  = 12;
    public static final int WORLD_HEIGHT = 11;
    /**
     * Tamano de celda en pixeles. 52 -> ventana de 624x572, que entra comoda
     * junto a los controles de Greenfoot. Subelo a 64 si tu pantalla es grande.
     */
    public static final int CELL_SIZE    = 52;
    public static final int OFFSET_X     = 2;
    public static final int OFFSET_Y     = 2;
    public static final int BOARD_SIZE   = 8;

    public static final int DEPLOY_ROW_A = OFFSET_Y + 6;   // y = 8
    public static final int DEPLOY_ROW_B = OFFSET_Y + 7;   // y = 9
    public static final int BENCH_ROW    = 10;

    private TurnManager turnManager   = new TurnManager();
    private EnemyAI enemyAI           = new EnemyAI();
    private Inventory inventory       = new Inventory();
    private RewardScreen rewardScreen = new RewardScreen();

    /** Party permanente del jugador. Permadeath: lo que sale de aqui no vuelve. */
    private List<Piece> playerRoster = new ArrayList<Piece>();

    private int level = 1;
    private int piecesLost = 0;
    private Piece selectedPiece = null;
    private HudPanel hud;
    private TurnIndicator turnIndicator;
    private GameButton startBtn = null;
    private String lastHudInfo = "";

    /** Animacion de desplazamiento en curso (como maximo una a la vez). */
    private MoveFx activeFx = null;
    /** Halo bajo la pieza que recibe el golpe durante el minijuego. */
    private TargetHalo halo = null;

    public ChessWorld()
    {
        super(WORLD_WIDTH, WORLD_HEIGHT, CELL_SIZE);

        // Orden de pintado (el primero queda arriba del todo). Los efectos se
        // intercalan a proposito: FloatingText y MoveFx por encima de las piezas
        // para que se lean siempre; DeathFx y TargetHalo por debajo, para que la
        // pieza que entra en la casilla capturada tape los restos del defensor.
        setPaintOrder(GameOverScreen.class, RewardCard.class, TimingBar.class,
                      HudPanel.class, TurnIndicator.class, GameButton.class,
                      FloatingText.class, ItemIcon.class, MoveFx.class,
                      Piece.class, DeathFx.class, TargetHalo.class, Tile.class);

        generarMapaCompleto();

        hud = new HudPanel();
        addObject(hud, 6, 0);

        turnIndicator = new TurnIndicator();
        addObject(turnIndicator, 2, 1);

        crearPartyInicial();
        crearInventarioInicial();
        startLevel();
    }

    // ==================================================================
    // CICLO PRINCIPAL
    // ==================================================================
    public void act()
    {
        GameState state = turnManager.getState();

        if (state == GameState.DEPLOY) {
            updateDeployButton();
        } else if (state == GameState.PLAYER_TURN) {
            handlePlayerInput();
        } else if (state == GameState.ENEMY_TURN) {
            enemyAI.update(this);
        }
        // GameState.ANIMATING no hace nada aqui a proposito: el MoveFx se anima
        // en su propio act() y avisa por onMoveFxFinished(). Mientras tanto ni el
        // jugador ni la IA pueden actuar, que es justo lo que se quiere.

        updateHud();
    }

    // ==================================================================
    // CONSTRUCCION DEL MAPA
    // ==================================================================
    private void generarMapaCompleto()
    {
        for (int y = 0; y < WORLD_HEIGHT; y++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                if (isInsideBoard(x, y)) {
                    int boardX = x - OFFSET_X;
                    int boardY = y - OFFSET_Y;
                    String tipo = ((boardX + boardY) % 2 == 0) ? "White" : "Black";
                    addObject(new Tile(tipo, x, y), x, y);
                } else {
                    addObject(new Tile("background", x, y), x, y);
                }
            }
        }
    }

    private void crearPartyInicial()
    {
        List<Piece> pool = new ArrayList<Piece>();
        pool.add(new KnightPiece(true));
        pool.add(new BishopPiece(true));
        pool.add(new RookPiece(true));
        pool.add(new PawnPiece(true));
        Collections.shuffle(pool);

        playerRoster.add(new KingPiece(true));   // El Rey siempre esta
        playerRoster.add(pool.get(0));
        playerRoster.add(pool.get(1));
        playerRoster.add(pool.get(2));
    }

    private void crearInventarioInicial()
    {
        inventory.add(new Weapon("Espada Oxidada", 6));
        inventory.add(new Armor("Casaca de Cuero", 18));
    }

    // ==================================================================
    // NIVELES
    // ==================================================================
    private void startLevel()
    {
        turnManager.setState(GameState.DEPLOY);
        selectedPiece = null;
        startBtn = null;

        PieceThemeManager.assignRandomColor();

        // Limpia el tablero de piezas e iconos del nivel anterior
        for (Piece p : getObjects(Piece.class))  removeObject(p);
        for (ItemIcon i : getObjects(ItemIcon.class)) removeObject(i);
        for (GameButton b : getObjects(GameButton.class)) removeObject(b);
        clearFx();
        clearHighlights();

        // Las piezas supervivientes vuelven a la banca para redesplegarse
        for (int i = 0; i < playerRoster.size(); i++) {
            Piece p = playerRoster.get(i);
            p.setDeployed(false);
            p.setSelected(false);
            p.setHidden(false);   // por si el nivel acabo con una animacion a medias
            int[] cell = benchCell(i);
            addObject(p, cell[0], cell[1]);
            p.setHome(cell[0], cell[1]);
        }

        LevelGenerator.spawnEnemies(this, level);
        PieceThemeManager.refreshAll(this);
        refreshInventoryIcons();
        markDeploymentZone(true);

        setMessage("Nivel " + level + ": arrastra los items sobre tus piezas y despliegalas en las 2 filas amarillas.");
    }

    /**
     * Posicion en la banca: primero la fila inferior (x 2..9) y, si la party
     * crece, las columnas libres de la derecha.
     */
    private int[] benchCell(int index)
    {
        if (index < BOARD_SIZE) {
            return new int[]{OFFSET_X + index, BENCH_ROW};
        }
        int j = index - BOARD_SIZE;
        return new int[]{10 + (j % 2), 2 + (j / 2)};
    }

    public void beginReward()
    {
        turnManager.setState(GameState.REWARD);
        clearFx();
        clearHighlights();
        if (selectedPiece != null) { selectedPiece.setSelected(false); selectedPiece = null; }
        setMessage("Nivel " + level + " superado. Elige tu recompensa.");
        rewardScreen.show(this, level);
    }

    public void chooseReward(RewardCard card)
    {
        if (RewardCard.KIND_ITEM.equals(card.getKind())) {
            inventory.add(card.getItem());
        } else if (RewardCard.KIND_PIECE.equals(card.getKind())) {
            playerRoster.add(card.getPiece());
        } else {
            for (Piece p : playerRoster) p.healFull();
        }

        rewardScreen.clear(this);

        // Curacion del 25% entre niveles (el "Descanso" ya curo del todo)
        for (Piece p : playerRoster) p.heal((int) Math.round(p.getMaxHp() * 0.25));

        level++;
        startLevel();
    }

    public void triggerGameOver()
    {
        turnManager.setState(GameState.GAME_OVER);
        clearFx();
        clearHighlights();
        selectedPiece = null;
        addObject(new GameOverScreen(level, piecesLost), 6, 5);
        setMessage("El Rey ha caido. Fin de la partida.");
    }

    // ==================================================================
    // FASE DE DESPLIEGUE
    // ==================================================================
    public boolean isValidDeploymentTile(int x, int y)
    {
        return isValidDeploymentTile(x, y, null);
    }

    /**
     * Valida una casilla de despliegue.
     *
     * IMPORTANTE: mientras se arrastra, la pieza YA esta sobre la celda destino,
     * asi que hay que excluirla del test de "casilla ocupada". Sin este parametro
     * toda pieza arrastrada se consideraba a si misma un obstaculo y rebotaba.
     */
    public boolean isValidDeploymentTile(int x, int y, Piece ignore)
    {
        boolean insideZone = (x >= OFFSET_X && x < OFFSET_X + BOARD_SIZE)
                          && (y >= DEPLOY_ROW_A && y <= DEPLOY_ROW_B);
        if (!insideZone) return false;

        for (Piece other : getObjectsAt(x, y, Piece.class)) {
            if (other != ignore) return false;
        }
        return true;
    }

    private void updateDeployButton()
    {
        boolean allDeployed = true;
        for (Piece p : playerRoster) {
            if (!p.isDeployed()) { allDeployed = false; break; }
        }

        if (allDeployed && startBtn == null) {
            startBtn = new GameButton("EMPEZAR COMBATE", "start");
            addObject(startBtn, 6, 1);
            setMessage("Piezas listas. Pulsa EMPEZAR COMBATE.");
        } else if (!allDeployed && startBtn != null) {
            removeObject(startBtn);
            startBtn = null;
        }
    }

    public void onButtonPressed(String action)
    {
        if ("start".equals(action) && turnManager.getState() == GameState.DEPLOY) {
            if (startBtn != null) { removeObject(startBtn); startBtn = null; }
            markDeploymentZone(false);
            turnManager.setState(GameState.PLAYER_TURN);
            addObject(new GameButton("PASAR TURNO", "pass"), 9, 1);
            setMessage("Tu turno: clic en una pieza y luego en su destino (verde mueve, rojo ataca).");
        }
        else if ("pass".equals(action) && turnManager.getState() == GameState.PLAYER_TURN) {
            clearSelection();
            setMessage("Pasas tu turno.");
            turnManager.endPlayerAction(this);
        }
    }

    private void markDeploymentZone(boolean on)
    {
        for (int x = OFFSET_X; x < OFFSET_X + BOARD_SIZE; x++) {
            for (int y = DEPLOY_ROW_A; y <= DEPLOY_ROW_B; y++) {
                Tile t = tileAt(x, y);
                if (t != null) t.setHighlight(on ? Tile.HL_DEPLOY : Tile.HL_NONE);
            }
        }
    }

    // ==================================================================
    // INVENTARIO
    // ==================================================================
    private void refreshInventoryIcons()
    {
        for (ItemIcon icon : getObjects(ItemIcon.class)) removeObject(icon);

        List<Item> items = inventory.getItems();
        for (int i = 0; i < items.size() && i < 18; i++) {
            ItemIcon icon = new ItemIcon(items.get(i));
            int x = i % 2;                 // columnas 0 y 1
            int y = 2 + (i / 2);           // filas 2..10
            if (y > 10) break;
            addObject(icon, x, y);
            icon.setHome(x, y);
        }
    }

    /** Equipa el item del icono en la pieza indicada y actualiza el panel. */
    public void equipItemOnPiece(ItemIcon icon, Piece piece)
    {
        Item item = icon.getItem();
        Item previous = piece.equipItem(item);

        inventory.remove(item);
        if (previous != null) inventory.add(previous);

        refreshInventoryIcons();
        setMessage(piece.getPieceType().toUpperCase() + " equipa " + item.getName()
                   + "  ->  HP " + piece.getMaxHp() + " / ATQ " + piece.getAttack()
                   + " / alcance " + piece.getMoveRange());
    }

    public void showItemInfo(Item item)
    {
        setMessage(item.getName() + ": " + item.getDescription());
    }

    // ==================================================================
    // TURNO DEL JUGADOR
    // ==================================================================
    private void handlePlayerInput()
    {
        if (!Greenfoot.mouseClicked(null)) return;
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse == null) return;

        int mx = mouse.getX();
        int my = mouse.getY();

        List<Piece> at = getObjectsAt(mx, my, Piece.class);

        // Clic sobre una pieza propia -> seleccionarla
        if (!at.isEmpty() && at.get(0).isPlayer()) {
            selectPiece(at.get(0));
            return;
        }

        if (selectedPiece == null) return;

        // Ataque
        if (selectedPiece.canAttack(mx, my)) {
            Piece target = at.get(0);
            Piece attacker = selectedPiece;
            clearSelection();
            startMinigame(attacker, target, TimingBar.Mode.ATTACK, mx, my);
            return;
        }

        // Movimiento
        if (selectedPiece.canMoveTo(mx, my)) {
            Piece mover = selectedPiece;
            clearSelection();
            setMessage("Mueves tu " + mover.getPieceType() + ".");
            startSlide(mover, mx, my, MoveFx.AFTER_PLAYER);
            return;
        }

        clearSelection();
    }

    private void selectPiece(Piece p)
    {
        if (selectedPiece != null) selectedPiece.setSelected(false);
        selectedPiece = p;
        p.setSelected(true);
        showMovesFor(p);
        setMessage(p.getPieceType().toUpperCase() + "  HP " + p.getHp() + "/" + p.getMaxHp()
                   + "  ATQ " + p.getAttack() + "  alcance " + p.getMoveRange());
    }

    private void clearSelection()
    {
        if (selectedPiece != null) selectedPiece.setSelected(false);
        selectedPiece = null;
        clearHighlights();
    }

    private void showMovesFor(Piece p)
    {
        clearHighlights();
        for (int y = OFFSET_Y; y < OFFSET_Y + BOARD_SIZE; y++) {
            for (int x = OFFSET_X; x < OFFSET_X + BOARD_SIZE; x++) {
                Tile t = tileAt(x, y);
                if (t == null) continue;
                if (p.canAttack(x, y))      t.setHighlight(Tile.HL_ATTACK);
                else if (p.canMoveTo(x, y)) t.setHighlight(Tile.HL_MOVE);
            }
        }
        Tile own = tileAt(p.getX(), p.getY());
        if (own != null) own.setHighlight(Tile.HL_SELECTED);
    }

    public void clearHighlights()
    {
        for (Tile t : getObjects(Tile.class)) t.setHighlight(Tile.HL_NONE);
    }

    private Tile tileAt(int x, int y)
    {
        List<Tile> tiles = getObjectsAt(x, y, Tile.class);
        return tiles.isEmpty() ? null : tiles.get(0);
    }

    // ==================================================================
    // COMBATE
    // ==================================================================
    public void startMinigame(Piece attacker, Piece defender, TimingBar.Mode mode, int tx, int ty)
    {
        turnManager.setState(GameState.MINIGAME);
        clearHighlights();

        // Halo bajo el objetivo: la barra tapa medio tablero y sin el no se sabe
        // sobre que pieza recaen las consecuencias del minijuego.
        if (halo != null) { removeObject(halo); halo = null; }
        if (defender != null && defender.getWorld() != null) {
            halo = new TargetHalo(defender.isPlayer());
            addObject(halo, defender.getX(), defender.getY());
        }

        addObject(new TimingBar(attacker, defender, mode, tx, ty, level), 6, 5);
    }

    // ==================================================================
    // ANIMACIONES
    // ==================================================================

    /**
     * Desliza una pieza hasta (tx,ty). El juego pasa a ANIMATING para que nadie
     * actue mientras dura, y cuando termina se ejecuta {@code after}
     * (AFTER_PLAYER o AFTER_ENEMY) para cerrar el turno.
     *
     * La pieza REAL no se mueve durante la animacion: se queda oculta en su
     * casilla de origen y solo salta al destino al final. Asi ninguna consulta
     * de tablero ve una pieza a medio camino.
     */
    public void startSlide(Piece piece, int tx, int ty, int after)
    {
        if (piece == null || piece.getWorld() == null) { dispatchAfter(after); return; }
        if (piece.getX() == tx && piece.getY() == ty)  { dispatchAfter(after); return; }

        // Salvaguarda: si la pieza venia de una embestida sin terminar seguiria
        // oculta y el nuevo MoveFx copiaria una imagen en blanco.
        for (MoveFx pending : getObjects(MoveFx.class)) {
            if (pending.getPiece() == piece) removeObject(pending);
        }
        piece.setHidden(false);

        int fromX = piece.getX();
        int fromY = piece.getY();

        MoveFx fx = new MoveFx(piece, fromX, fromY, tx, ty, after);  // copia la imagen actual
        piece.setHidden(true);
        addObject(fx, fromX, fromY);
        activeFx = fx;

        turnManager.setState(GameState.ANIMATING);
    }

    /** Lo llama el propio MoveFx al terminar su recorrido. */
    public void onMoveFxFinished(MoveFx fx)
    {
        if (fx == null) return;
        Piece piece = fx.getPiece();

        removeObject(fx);
        if (activeFx == fx) activeFx = null;

        if (piece != null) {
            piece.setHidden(false);
            if (fx.isSlide() && piece.getWorld() != null) {
                piece.moveTo(fx.getDestX(), fx.getDestY());
            }
        }

        dispatchAfter(fx.getAfter());
    }

    /**
     * Efectos del impacto: embestida del atacante, temblor y destello del
     * defensor, y el numero flotante. Lo dispara TimingBar al resolverse.
     * El retardo del impacto hace que el golpe se vea cuando la embestida llega.
     */
    public void playHitFx(Piece attacker, Piece defender, int tx, int ty,
                          int damage, String text, Color color)
    {
        int impact = 9;   // mitad de la embestida

        if (attacker != null && attacker.getWorld() != null && !attacker.isHidden()) {
            MoveFx lunge = new MoveFx(attacker, tx, ty);   // copia la imagen actual
            int ax = attacker.getX(), ay = attacker.getY();
            attacker.setHidden(true);
            addObject(lunge, ax, ay);
        }

        if (defender != null && defender.getWorld() != null) {
            if (damage > 0) defender.hitReaction(impact);
            addObject(new FloatingText(text, color, impact), defender.getX(), defender.getY());
        }
    }

    /** Cierra el turno segun el codigo AFTER_* del MoveFx. */
    private void dispatchAfter(int after)
    {
        if (after == MoveFx.AFTER_PLAYER)     turnManager.endPlayerAction(this);
        else if (after == MoveFx.AFTER_ENEMY) turnManager.endEnemyAction(this);
    }

    /** Borra todos los actores decorativos. Se llama al cambiar de fase. */
    private void clearFx()
    {
        for (MoveFx f : getObjects(MoveFx.class)) {
            if (f.getPiece() != null) f.getPiece().setHidden(false);
            removeObject(f);
        }
        for (FloatingText f : getObjects(FloatingText.class)) removeObject(f);
        for (DeathFx f : getObjects(DeathFx.class))           removeObject(f);
        for (TargetHalo f : getObjects(TargetHalo.class))     removeObject(f);
        activeFx = null;
        halo = null;
    }

    /**
     * Resultado del minijuego. Aplica las consecuencias (muerte, permadeath,
     * captura estilo ajedrez) y devuelve el turno a quien corresponda.
     */
    public void onCombatResolved(Piece attacker, Piece defender, boolean died,
                                 int tx, int ty, TimingBar.Mode mode)
    {
        if (halo != null) { removeObject(halo); halo = null; }

        boolean gameOver = false;
        int after = (mode == TimingBar.Mode.ATTACK) ? MoveFx.AFTER_PLAYER : MoveFx.AFTER_ENEMY;

        if (died) {
            if (defender.isPlayer()) {
                playerRoster.remove(defender);   // PERMADEATH
                piecesLost++;
                if (defender.isKing()) gameOver = true;
                setMessage("Has perdido tu " + defender.getPieceType() + " para siempre.");
            } else {
                setMessage("Enemigo " + defender.getPieceType() + " eliminado.");
            }

            // La pieza sale del mundo YA (la logica no puede esperar animaciones);
            // lo que se desvanece en la casilla es solo una copia de su imagen.
            addObject(new DeathFx(defender.getImage()), tx, ty);
            removeObject(defender);

            if (gameOver) { triggerGameOver(); return; }

            // Captura estilo ajedrez: la pieza atacante entra en la casilla.
            if (attacker != null && attacker.getWorld() != null) {
                startSlide(attacker, tx, ty, after);
                return;
            }
        }

        dispatchAfter(after);
    }

    // ==================================================================
    // CONSULTAS
    // ==================================================================
    public boolean isInsideBoard(int x, int y)
    {
        return x >= OFFSET_X && x < OFFSET_X + BOARD_SIZE
            && y >= OFFSET_Y && y < OFFSET_Y + BOARD_SIZE;
    }

    public List<Piece> getPlayerPieces()
    {
        List<Piece> out = new ArrayList<Piece>();
        for (Piece p : getObjects(Piece.class)) if (p.isPlayer()) out.add(p);
        return out;
    }

    public List<Piece> getEnemyPieces()
    {
        List<Piece> out = new ArrayList<Piece>();
        for (Piece p : getObjects(Piece.class)) if (!p.isPlayer()) out.add(p);
        return out;
    }

    private void updateHud()
    {
        String turno;
        GameState s = turnManager.getState();
        if (s == GameState.DEPLOY)           turno = "DESPLIEGUE";
        else if (s == GameState.PLAYER_TURN) turno = "TU TURNO";
        else if (s == GameState.MINIGAME)    turno = "COMBATE";
        else if (s == GameState.ANIMATING)   turno = "MOVIENDO...";
        else if (s == GameState.ENEMY_TURN)  turno = "TURNO IA";
        else if (s == GameState.REWARD)      turno = "RECOMPENSA";
        else                                 turno = "GAME OVER";

        updateTurnIndicator(s, turno);

        String color = PieceThemeManager.isPlayerWhite ? "BLANCAS" : "NEGRAS";
        String info = "NIVEL " + level + "   |   " + turno + "   |   Juegas: " + color
                    + "   |   Tus piezas: " + playerRoster.size()
                    + "   |   Enemigos: " + getEnemyPieces().size()
                    + "   |   Perdidas: " + piecesLost;

        if (!info.equals(lastHudInfo)) {
            lastHudInfo = info;
            hud.setInfo(info);
        }
    }

    /**
     * El cartel late solo cuando el juego espera una accion del jugador.
     * El latido es la respuesta a "¿me toca a mi?"; su ausencia tambien informa.
     */
    private void updateTurnIndicator(GameState s, String turno)
    {
        if (turnIndicator == null) return;

        Color c;
        boolean pulsing;
        if (s == GameState.PLAYER_TURN)      { c = new Color(120, 240, 140); pulsing = true;  }
        else if (s == GameState.DEPLOY)      { c = new Color(255, 225, 120); pulsing = true;  }
        else if (s == GameState.MINIGAME)    { c = new Color(255, 200, 120); pulsing = true;  }
        else if (s == GameState.REWARD)      { c = new Color(200, 180, 255); pulsing = true;  }
        else if (s == GameState.GAME_OVER)   { c = new Color(255, 110, 110); pulsing = false; }
        else                                 { c = new Color(235, 140, 110); pulsing = false; }

        turnIndicator.show(turno, c, pulsing);
    }

    public void setMessage(String msg) { hud.setMessage(msg); }

    public GameState getState()        { return turnManager.getState(); }
    public TurnManager getTurnManager(){ return turnManager; }
    public EnemyAI getEnemyAI()        { return enemyAI; }
    public Inventory getInventory()    { return inventory; }
    public int getLevel()              { return level; }
    public List<Piece> getPlayerRoster(){ return playerRoster; }

    // Compatibilidad con el codigo anterior
    public boolean isGameStarted()   { return turnManager.getState() != GameState.DEPLOY; }
    public boolean isMinigameActive(){ return turnManager.getState() == GameState.MINIGAME; }
}
