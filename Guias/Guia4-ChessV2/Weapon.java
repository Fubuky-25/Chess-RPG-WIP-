import greenfoot.*;

/** Arma: aumenta el ataque de la pieza. */
public class Weapon extends Item
{
    private int attackBonus;

    public Weapon(String name, int attackBonus)
    {
        super(name, "+" + attackBonus + " ATQ");
        this.attackBonus = attackBonus;
    }

    @Override
    public String getSlot() { return SLOT_WEAPON; }

    @Override
    public void applyTo(Piece piece) { piece.addAttackBonus(attackBonus); }

    @Override
    public GreenfootImage createIcon() { return baseIcon(new Color(150, 40, 40), "A"); }

    public int getAttackBonus() { return attackBonus; }
}
