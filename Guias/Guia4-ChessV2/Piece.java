import greenfoot.*;
import java.util.List;

/**
 * Clase base abstracta de todas las piezas (jugador y enemigas).
 *
 * HERENCIA / POLIMORFISMO:
 *   - isValidMove()   lo define cada subclase (patron geometrico + alcance).
 *   - isValidAttack() por defecto es igual a isValidMove(); solo el Peon lo cambia.
 *   - jumpsOverPieces() solo el Caballo lo pone en true.
 *
 * ENCAPSULAMIENTO:
 *   los stats base son privados/protegidos y el valor efectivo (base + equipo)
 *   se obtiene siempre con los getters, nunca leyendo los campos desde fuera.
 */
public abstract class Piece extends Actor
{
    protected String pieceType;

    // Stats BASE (sin equipamiento)
    protected int baseMaxHp;
    protected int baseAttack;
    protected int baseMoveRange;

    // Stats EFECTIVOS (base + equipamiento). Se recalculan con recalculateStats()
    private int maxHp;
    private int attack;
    private int moveRange;
    private int critMultiplier;

    private int hp;
    protected boolean isPlayer;

    // Slots de equipamiento
    private Item weaponSlot;
    private Item armorSlot;
    private Item relicSlot;

    // Posicion logica: si la pieza se desfasa (arrastre en modo edicion) vuelve aqui
    private int homeX = -1;
    private int homeY = -1;

    private boolean isDeployed = false;
    private boolean isDragging = false;
    private boolean selected = false;

    // ---- Estado puramente visual (animaciones). Nunca afecta a la logica ----
    /** Sprite cacheado: cargar y escalar el PNG en cada refresco seria carisimo
     *  ahora que la imagen se rehace en cada ciclo mientras dura una sacudida. */
    private GreenfootImage spriteCache = null;
    /** Oculta la pieza sin sacarla del mundo, mientras un MoveFx la representa.
     *  La imagen sigue midiendo una celda para no alterar ninguna consulta. */
    private boolean hidden = false;
    private int shakeTicks = 0;    // temblor tras encajar un golpe
    private int flashTicks = 0;    // destello rojo tras encajar un golpe
    private int hitDelay = 0;      // espera para que el impacto coincida con la embestida

    public Piece(String pieceType, int maxHp, int attack, int moveRange, boolean isPlayer)
    {
        this.pieceType = pieceType;
        this.baseMaxHp = maxHp;
        this.baseAttack = attack;
        this.baseMoveRange = moveRange;
        this.isPlayer = isPlayer;
        recalculateStats();
        this.hp = this.maxHp;
        refreshImage();
    }

    // ------------------------------------------------------------------
    // CICLO DE VIDA
    // ------------------------------------------------------------------
    public void act()
    {
        World w = getWorld();
        if (w == null) return;                 // FIX: antes se casteaba antes de comprobar null
        if (!(w instanceof ChessWorld)) return;
        ChessWorld world = (ChessWorld) w;

        updateHitFx();

        GameState state = world.getState();

        // Inmovilidad: si la pieza se desfaso y no se esta arrastrando, vuelve a su celda
        if (!isDragging && homeX >= 0 && (getX() != homeX || getY() != homeY)) {
            setLocation(homeX, homeY);
        }

        // Drag & Drop SOLO en fase de despliegue y solo para piezas del jugador
        if (state == GameState.DEPLOY && isPlayer) {
            handleDragAndDrop(world);
        }
        // En combate la seleccion la gestiona ChessWorld con clics tacticos.
    }

    private void handleDragAndDrop(ChessWorld world)
    {
        // Greenfoot entrega como maximo un evento de mouse por ciclo,
        // por eso se comprueban en este orden y se sale tras cada uno.

        if (Greenfoot.mouseDragEnded(this)) {
            isDragging = false;
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null && world.isValidDeploymentTile(mouse.getX(), mouse.getY(), this)) {
                moveTo(mouse.getX(), mouse.getY());
                isDeployed = true;
            } else {
                setLocation(homeX, homeY);
            }
            return;
        }

        if (isDragging && Greenfoot.mouseDragged(this)) {
            MouseInfo mouse = Greenfoot.getMouseInfo();
            if (mouse != null) setLocation(mouse.getX(), mouse.getY());
            return;
        }

        if (Greenfoot.mousePressed(this)) {
            isDragging = true;
            return;
        }

        // Clic sin arrastre: cancela el estado de arrastre para no quedar bloqueado
        if (isDragging && Greenfoot.mouseClicked(null)) {
            isDragging = false;
            setLocation(homeX, homeY);
        }
    }

    // ------------------------------------------------------------------
    // MOVIMIENTO: patron (polimorfico) + reglas del tablero (comunes)
    // ------------------------------------------------------------------

    /** Patron geometrico de movimiento. Lo define cada subclase. */
    public abstract boolean isValidMove(int targetX, int targetY);

    /** Patron de ataque. Por defecto igual al de movimiento (solo el Peon lo cambia). */
    public boolean isValidAttack(int targetX, int targetY)
    {
        return isValidMove(targetX, targetY);
    }

    /** Solo el Caballo salta por encima de otras piezas. */
    public boolean jumpsOverPieces() { return false; }

    /** Movimiento completo: patron + dentro del tablero + camino libre + casilla vacia. */
    public boolean canMoveTo(int tx, int ty)
    {
        ChessWorld world = getChessWorld();
        if (world == null) return false;
        if (!world.isInsideBoard(tx, ty)) return false;
        if (tx == getX() && ty == getY()) return false;
        if (!isValidMove(tx, ty)) return false;
        if (!world.getObjectsAt(tx, ty, Piece.class).isEmpty()) return false;
        return isPathClear(tx, ty);
    }

    /** Ataque completo: patron de ataque + camino libre + hay un rival en destino. */
    public boolean canAttack(int tx, int ty)
    {
        ChessWorld world = getChessWorld();
        if (world == null) return false;
        if (!world.isInsideBoard(tx, ty)) return false;
        if (!isValidAttack(tx, ty)) return false;
        List<Piece> at = world.getObjectsAt(tx, ty, Piece.class);
        if (at.isEmpty()) return false;
        if (at.get(0).isPlayer() == this.isPlayer) return false;
        return isPathClear(tx, ty);
    }

    /** Recorre la linea entre origen y destino comprobando que no haya piezas. */
    protected boolean isPathClear(int tx, int ty)
    {
        if (jumpsOverPieces()) return true;
        ChessWorld world = getChessWorld();
        if (world == null) return false;

        int stepX = Integer.signum(tx - getX());
        int stepY = Integer.signum(ty - getY());
        int x = getX() + stepX;
        int y = getY() + stepY;

        int guard = 0;
        while ((x != tx || y != ty) && guard < 32) {
            if (!world.getObjectsAt(x, y, Piece.class).isEmpty()) return false;
            x += stepX;
            y += stepY;
            guard++;
        }
        return true;
    }

    // Helpers geometricos reutilizados por las subclases
    protected boolean isStraightLine(int dx, int dy) { return (dx == 0) ^ (dy == 0); }
    protected boolean isDiagonalLine(int dx, int dy) { return dx != 0 && Math.abs(dx) == Math.abs(dy); }
    protected int chebyshev(int dx, int dy) { return Math.max(Math.abs(dx), Math.abs(dy)); }

    // ------------------------------------------------------------------
    // COMBATE Y SALUD
    // ------------------------------------------------------------------

    /** Aplica dano y devuelve true si la pieza queda con 0 HP. El mundo decide que hacer. */
    public boolean applyDamage(int amount)
    {
        if (amount < 0) amount = 0;
        hp -= amount;
        if (hp < 0) hp = 0;
        refreshImage();
        return hp <= 0;
    }

    public void heal(int amount)
    {
        hp = Math.min(maxHp, hp + amount);
        refreshImage();
    }

    public void healFull()
    {
        hp = maxHp;
        refreshImage();
    }

    // ------------------------------------------------------------------
    // EQUIPAMIENTO
    // ------------------------------------------------------------------

    /** Equipa un item. Devuelve el item que estaba en ese slot (o null). */
    public Item equipItem(Item item)
    {
        if (item == null) return null;
        Item previous = null;
        String slot = item.getSlot();

        if (Item.SLOT_WEAPON.equals(slot))      { previous = weaponSlot; weaponSlot = item; }
        else if (Item.SLOT_ARMOR.equals(slot))  { previous = armorSlot;  armorSlot  = item; }
        else                                    { previous = relicSlot;  relicSlot  = item; }

        int oldMax = maxHp;
        recalculateStats();
        // Subir maxHp con una armadura otorga esa vida al instante (no cura el resto)
        if (maxHp > oldMax) hp += (maxHp - oldMax);
        if (hp > maxHp) hp = maxHp;
        refreshImage();
        return previous;
    }

    /**
     * Recalcula los stats efectivos: parte de los base y deja que cada item
     * aplique su modificador (POLIMORFISMO: cada Item sabe que hacer).
     */
    public void recalculateStats()
    {
        maxHp = baseMaxHp;
        attack = baseAttack;
        moveRange = baseMoveRange;
        critMultiplier = 2;

        if (weaponSlot != null) weaponSlot.applyTo(this);
        if (armorSlot  != null) armorSlot.applyTo(this);
        if (relicSlot  != null) relicSlot.applyTo(this);
    }

    // Metodos que usan los items para modificar la pieza
    public void addAttackBonus(int v)   { attack += v; }
    public void addMaxHpBonus(int v)    { maxHp += v; }
    public void setMoveRangeValue(int v){ moveRange = v; }
    public void setCritMultiplier(int v){ critMultiplier = v; }

    /** Escalado procedural de dificultad para los enemigos. */
    public void applyLevelScaling(double multiplier)
    {
        baseMaxHp  = (int) Math.round(baseMaxHp * multiplier);
        baseAttack = (int) Math.round(baseAttack * multiplier);
        recalculateStats();
        hp = maxHp;
        refreshImage();
    }

    // ------------------------------------------------------------------
    // IMAGEN
    // ------------------------------------------------------------------
    /**
     * Redibuja la pieza. IMPORTANTE: la imagen mide SIEMPRE una celda exacta,
     * incluso oculta o temblando. Greenfoot resuelve getObjectsAt() mirando si
     * la imagen del actor cubre el centro de la casilla, asi que una imagen mas
     * grande haria que la pieza apareciera en las casillas vecinas y romperia
     * el bloqueo de camino, las capturas y el despliegue. El temblor se dibuja
     * desplazando el sprite DENTRO de la celda, no agrandando el lienzo.
     */
    public void refreshImage()
    {
        int size = ChessWorld.CELL_SIZE;
        GreenfootImage img = new GreenfootImage(size, size);

        if (hidden) { setImage(img); return; }

        GreenfootImage sprite = sprite();

        if (selected) {
            img.setColor(new Color(255, 235, 130, 150));
            img.fillOval(4, 8, size - 8, size - 12);
        }

        int shakeX = 0;
        if (shakeTicks > 0) {
            int amp = 1 + shakeTicks / 5;
            shakeX = ((shakeTicks / 2) % 2 == 0) ? amp : -amp;
        }
        img.drawImage(sprite, (size - sprite.getWidth()) / 2 + shakeX,
                              (size - sprite.getHeight()) / 2 + 3);

        if (flashTicks > 0) {
            int alpha = Math.min(150, 25 + flashTicks * 9);
            img.setColor(new Color(255, 70, 70, alpha));
            img.fillOval(5, 7, size - 10, size - 12);
        }

        // Barra de HP dibujada sobre el sprite (reemplaza los showText del codigo anterior)
        int barW = size - 18, barH = 5, bx = (size - barW) / 2, by = 2;
        img.setColor(Color.BLACK);
        img.fillRect(bx - 1, by - 1, barW + 2, barH + 2);
        img.setColor(new Color(90, 20, 20));
        img.fillRect(bx, by, barW, barH);
        double ratio = (maxHp <= 0) ? 0 : (double) hp / (double) maxHp;
        if (ratio < 0) ratio = 0;
        img.setColor(isPlayer ? new Color(60, 210, 90) : new Color(235, 120, 40));
        img.fillRect(bx, by, (int) Math.round(barW * ratio), barH);

        setImage(img);
    }

    public void setSelected(boolean s)
    {
        if (s != selected) { selected = s; refreshImage(); }
    }

    // ------------------------------------------------------------------
    // EFECTOS VISUALES
    // ------------------------------------------------------------------

    /** Sprite cacheado. Se invalida cuando cambia el bando visual del nivel. */
    private GreenfootImage sprite()
    {
        if (spriteCache == null) spriteCache = PieceThemeManager.getSprite(pieceType, isPlayer);
        return spriteCache;
    }

    /** Fuerza a recargar el sprite (lo llama PieceThemeManager al re-sortear color). */
    public void invalidateSprite() { spriteCache = null; }

    /**
     * Oculta o muestra la pieza sin sacarla del mundo. Se usa mientras un MoveFx
     * dibuja su desplazamiento: la pieza sigue ocupando su casilla de origen.
     */
    public void setHidden(boolean h)
    {
        if (h != hidden) { hidden = h; refreshImage(); }
    }

    public boolean isHidden() { return hidden; }

    /**
     * Reaccion al encajar un golpe: temblor y destello rojo.
     * @param delay ciclos de espera, para que el impacto coincida con el momento
     *              en que la embestida del atacante llega al objetivo.
     */
    public void hitReaction(int delay)
    {
        if (delay > 0) { hitDelay = delay; return; }
        hitDelay = 0;
        shakeTicks = 14;
        flashTicks = 14;
        refreshImage();
    }

    /** Avanza el temblor y el destello. Lo llama act() en cualquier estado. */
    private void updateHitFx()
    {
        if (hitDelay > 0) {
            hitDelay--;
            if (hitDelay == 0) hitReaction(0);
            return;
        }
        if (shakeTicks > 0 || flashTicks > 0) {
            if (shakeTicks > 0) shakeTicks--;
            if (flashTicks > 0) flashTicks--;
            refreshImage();
        }
    }

    // ------------------------------------------------------------------
    // POSICION LOGICA
    // ------------------------------------------------------------------
    public void moveTo(int x, int y)
    {
        setLocation(x, y);
        homeX = x;
        homeY = y;
    }

    public void setHome(int x, int y) { homeX = x; homeY = y; }

    protected ChessWorld getChessWorld()
    {
        World w = getWorld();
        if (w instanceof ChessWorld) return (ChessWorld) w;
        return null;
    }

    // ------------------------------------------------------------------
    // GETTERS
    // ------------------------------------------------------------------
    public int getHp()             { return hp; }
    public int getMaxHp()          { return maxHp; }
    public int getAttack()         { return attack; }
    public int getMoveRange()      { return moveRange; }
    public int getCritMultiplier() { return critMultiplier; }
    public boolean isPlayer()      { return isPlayer; }
    public boolean isDeployed()    { return isDeployed; }
    public void setDeployed(boolean d) { isDeployed = d; }
    public String getPieceType()   { return pieceType; }
    public boolean isKing()        { return "king".equalsIgnoreCase(pieceType); }
    public Item getWeaponSlot()    { return weaponSlot; }
    public Item getArmorSlot()     { return armorSlot; }
    public Item getRelicSlot()     { return relicSlot; }
}
