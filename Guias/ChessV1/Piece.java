import greenfoot.*;

public abstract class Piece extends Actor {
    protected String pieceType;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected boolean isPlayer;
    protected boolean isDeployed = false;

    // Control de arrastre (Drag and Drop exclusivo pre-juego)
    private boolean isDragging = false;
    private int originalX;
    private int originalY;

    public Piece(String pieceType, int maxHp, int attack, boolean isPlayer) {
        this.pieceType = pieceType;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.isPlayer = isPlayer;
        
        PieceThemeManager.applyImage(this, pieceType, isPlayer);
    }

    public void act() {
        ChessWorld world = (ChessWorld) getWorld();
        if (world.isMinigameActive()) return;

        // 1. Bloqueo en Modo Edición
// ✅ CÓDIGO CORREGIDO:
// Se consulta si el entorno está ejecutándose usando Greenfoot.mouseClicked o el estado del mundo
if (getWorld() == null) return;

// Si intentan mover la pieza en la interfaz sin presionar RUN (modo edición), la regresa
if (isDeployed && !world.isGameStarted() && !isDragging) {
    if (getX() != originalX || getY() != originalY) {
        setLocation(originalX, originalY);
    }
}

        // 2. FASE PRE-JUEGO: Solo aquí funciona el Drag and Drop
        if (!world.isGameStarted() && isPlayer) {
            handleDragAndDrop(world);
            return;
        }

        // 3. FASE DE COMBATE: Clic en la pieza para seleccionarla
        if (world.isGameStarted() && isPlayer && Greenfoot.mouseClicked(this)) {
            world.selectPiece(this);
        }
    }

    private void handleDragAndDrop(ChessWorld world) {
        // Al presionar sobre la pieza, guardamos su origen
        if (Greenfoot.mousePressed(this)) {
            isDragging = true;
            originalX = getX();
            originalY = getY();
        }

        // Mientras se arrastra, sigue al puntero
        if (isDragging && Greenfoot.mouseDragged(this)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null) {
                setLocation(mouse.getX(), mouse.getY());
            }
        }

        // Al soltar el clic, valida la casilla
        if (isDragging && Greenfoot.mouseDragEnded(this)) {
            isDragging = false;
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null) {
                int dropX = mouse.getX();
                int dropY = mouse.getY();

                // Si se suelta en una casilla válida de despliegue
                if (world.isValidDeploymentTile(dropX, dropY)) {
                    setLocation(dropX, dropY);
                    originalX = dropX;
                    originalY = dropY;

                    if (!isDeployed) {
                        isDeployed = true;
                        world.incrementDeployedCount();
                    }
                } else {
                    // Si la casilla no es válida, regresa
                    setLocation(originalX, originalY);
                }
            }
        }
    }

    public abstract boolean isValidMove(int targetX, int targetY);

    public void takeDamage(int amount) {
        this.hp -= amount;
        getWorld().showText(pieceType + " HP: " + Math.max(0, hp), getX(), getY());
        if (this.hp <= 0) die();
    }

    protected void die() {
        ChessWorld world = (ChessWorld) getWorld();
        if (isPlayer && pieceType.equalsIgnoreCase("king")) {
            world.showText("¡GAME OVER!", 6, 1);
        }
        world.removeObject(this);
    }

    public int getAttack() { return attack; }
    public boolean isPlayer() { return isPlayer; }
    public boolean isDeployed() { return isDeployed; }
    public String getPieceType() { return pieceType; }
}