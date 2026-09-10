import greenfoot.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ChessWorld extends World {
    // Dimensiones totales de la ventana
    private static final int WORLD_WIDTH = 12;
    private static final int WORLD_HEIGHT = 11;
    private static final int CELL_SIZE = 64;

    // Desplazamiento para centrar el tablero 8x8 (Columna 3 a 10, Fila 3 a 10 -> X: 2..9, Y: 2..9)
    private static final int OFFSET_X = 2; 
    private static final int OFFSET_Y = 2; 

    private boolean minigameActive = false;
    private boolean gameStarted = false;
    
    private Piece selectedActivePiece = null;
    private int deployedCount = 0;
    private StartButton startBtn = null;

    public ChessWorld() {    
        super(WORLD_WIDTH, WORLD_HEIGHT, CELL_SIZE); 
        
        // 1. Asigna color aleatorio (Blancas o Negras) al jugador
        PieceThemeManager.assignRandomColor();
        
        // 2. Genera el tablero 8x8 rodeado por casillas background
        generarMapaCompleto();
        
        // 3. Coloca el texto y la reserva de 4 piezas al azar abajo
        prepararSeleccionInicial();
    }

    public void act() {
        // En fase de combate, gestiona la selección y movimiento por clics
        if (gameStarted && Greenfoot.mouseClicked(null)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null) {
                int mx = mouse.getX();
                int my = mouse.getY();
                
                if (isInsideBoard(mx, my)) {
                    tryMoveSelectedPiece(mx, my);
                }
            }
        }
    }

    /**
     * Construye la cuadrícula entera de 12x11:
     * - Tablero 8x8 (X: 2..9, Y: 2..9) -> Casillas "White" y "Black"
     * - Resto de casillas -> "background"
     */
    private void generarMapaCompleto() {
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

    private void prepararSeleccionInicial() {
        String colorTexto = PieceThemeManager.isPlayerWhite ? "BLANCAS" : "NEGRAS";
        showText("Juegas con " + colorTexto + ". Arrastra las 4 piezas a las 2 filas inferiores del tablero.", 6, 0);

        List<Piece> pool = new ArrayList<>();
        pool.add(new KnightPiece(true));
        pool.add(new BishopPiece(true));
        pool.add(new RookPiece(true));
        pool.add(new PawnPiece(true));
        
        Collections.shuffle(pool);

        List<Piece> equipoInicial = new ArrayList<>();
        equipoInicial.add(new KingPiece(true)); // El Rey siempre garantizado
        equipoInicial.add(pool.get(0));
        equipoInicial.add(pool.get(1));
        equipoInicial.add(pool.get(2));

        // Muestra la reserva de piezas abajo en la zona del background (Fila Y = 10)
        for (int i = 0; i < equipoInicial.size(); i++) {
            addObject(equipoInicial.get(i), i + 4, 10);
        }

        // Enemigo posicionado arriba en el tablero
        addObject(new PawnPiece(false), OFFSET_X + 3, OFFSET_Y + 1);
    }

    /**
     * Valida si la casilla destino está en las 2 filas inferiores del tablero (Y: 8 u 9)
     * y si no hay otra pieza ocupándola.
     */
    public boolean isValidDeploymentTile(int x, int y) {
        boolean insideDeploymentZone = (x >= OFFSET_X && x < OFFSET_X + 8) && (y >= OFFSET_Y + 6 && y <= OFFSET_Y + 7);
        boolean isEmpty = getObjectsAt(x, y, Piece.class).isEmpty();
        return insideDeploymentZone && isEmpty;
    }

    public void incrementDeployedCount() {
        deployedCount++;
        // Al colocar las 4 piezas, aparece el botón para iniciar la partida
        if (deployedCount >= 4 && startBtn == null) {
            startBtn = new StartButton();
            addObject(startBtn, 6, 1);
            showText("¡Piezas listas! Presiona el botón para comenzar.", 6, 0);
        }
    }

    public void startGameFromButton() {
        gameStarted = true;
        if (startBtn != null) {
            removeObject(startBtn);
        }
        showText("", 6, 0);
        showText("¡COMBATE INICIADO! Haz clic en una pieza y luego en su destino.", 6, 0);
    }

    private boolean isInsideBoard(int x, int y) {
        return (x >= OFFSET_X && x < OFFSET_X + 8 && y >= OFFSET_Y && y < OFFSET_Y + 8);
    }

    public void selectPiece(Piece p) {
        this.selectedActivePiece = p;
    }

    public void tryMoveSelectedPiece(int x, int y) {
        if (selectedActivePiece != null) {
            if (selectedActivePiece.isValidMove(x, y)) {
                List<Piece> targets = getObjectsAt(x, y, Piece.class);
                
                if (!targets.isEmpty()) {
                    Piece target = targets.get(0);
                    if (!target.isPlayer()) {
                        // Desencadena el minijuego de combate contra el enemigo
                        startMinigame(selectedActivePiece, target);
                    }
                } else {
                    selectedActivePiece.setLocation(x, y);
                }
                selectedActivePiece = null;
            }
        }
    }

    public boolean isGameStarted() { return gameStarted; }
    public boolean isMinigameActive() { return minigameActive; }

    public void startMinigame(Piece attacker, Piece defender) {
        minigameActive = true;
        addObject(new TimingBar(attacker, defender), 6, 5);
    }

    public void endMinigame() { minigameActive = false; }
}