import greenfoot.*;

/**
 * Reliquia: efectos especiales que no son solo +ataque o +vida.
 * Aqui vive el item que desbloquea el movimiento de ajedrez completo.
 */
public class Relic extends Item
{
    public static final String EFFECT_GRANDMASTER = "grandmaster"; // alcance completo
    public static final String EFFECT_FURY        = "fury";        // criticos x3
    public static final String EFFECT_VIGOR       = "vigor";       // +HP y +ATQ moderados

    private String effect;

    public Relic(String name, String effect, String description)
    {
        super(name, description);
        this.effect = effect;
    }

    @Override
    public String getSlot() { return SLOT_RELIC; }

    @Override
    public void applyTo(Piece piece)
    {
        if (EFFECT_GRANDMASTER.equals(effect)) {
            // Desbloquea el movimiento de ajedrez completo (todo el tablero)
            piece.setMoveRangeValue(ChessWorld.BOARD_SIZE);
        } else if (EFFECT_FURY.equals(effect)) {
            piece.setCritMultiplier(3);
        } else if (EFFECT_VIGOR.equals(effect)) {
            piece.addMaxHpBonus(20);
            piece.addAttackBonus(4);
        }
    }

    @Override
    public GreenfootImage createIcon() { return baseIcon(new Color(110, 55, 150), "R"); }

    public String getEffect() { return effect; }
}
