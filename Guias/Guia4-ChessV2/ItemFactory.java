import greenfoot.*;

/** Fabrica de items: centraliza el catalogo y el sorteo de recompensas. */
public class ItemFactory
{
    public static Item randomItem(int level)
    {
        int roll = Greenfoot.getRandomNumber(100);
        if (roll < 40)  return randomWeapon(level);
        if (roll < 75)  return randomArmor(level);
        return randomRelic();
    }

    public static Weapon randomWeapon(int level)
    {
        int roll = Greenfoot.getRandomNumber(3);
        if (roll == 0) return new Weapon("Espada Oxidada", 5 + level);
        if (roll == 1) return new Weapon("Lanza de Torneo", 8 + level);
        return new Weapon("Hacha de Guerra", 12 + level);
    }

    public static Armor randomArmor(int level)
    {
        int roll = Greenfoot.getRandomNumber(3);
        if (roll == 0) return new Armor("Casaca de Cuero", 15 + level * 2);
        if (roll == 1) return new Armor("Cota de Malla", 25 + level * 2);
        return new Armor("Coraza de Torre", 40 + level * 2);
    }

    public static Relic randomRelic()
    {
        int roll = Greenfoot.getRandomNumber(3);
        if (roll == 0) return new Relic("Manual del Gran Maestro", Relic.EFFECT_GRANDMASTER,
                                        "Movimiento de ajedrez completo");
        if (roll == 1) return new Relic("Amuleto de Furia", Relic.EFFECT_FURY,
                                        "Criticos x3 en vez de x2");
        return new Relic("Talisman de Vigor", Relic.EFFECT_VIGOR, "+20 HP y +4 ATQ");
    }

    /** Pieza de recompensa (nunca un segundo Rey). */
    public static Piece randomRewardPiece()
    {
        int roll = Greenfoot.getRandomNumber(4);
        if (roll == 0) return new KnightPiece(true);
        if (roll == 1) return new BishopPiece(true);
        if (roll == 2) return new RookPiece(true);
        return new PawnPiece(true);
    }
}
