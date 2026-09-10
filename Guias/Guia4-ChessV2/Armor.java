import greenfoot.*;

/** Armadura: aumenta la vida maxima de la pieza. */
public class Armor extends Item
{
    private int hpBonus;

    public Armor(String name, int hpBonus)
    {
        super(name, "+" + hpBonus + " HP");
        this.hpBonus = hpBonus;
    }

    @Override
    public String getSlot() { return SLOT_ARMOR; }

    @Override
    public void applyTo(Piece piece) { piece.addMaxHpBonus(hpBonus); }

    @Override
    public GreenfootImage createIcon() { return baseIcon(new Color(45, 80, 160), "D"); }

    public int getHpBonus() { return hpBonus; }
}
