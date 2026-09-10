import greenfoot.*;
import java.util.List;

/** Pantalla de recompensas: genera y limpia las 3 cartas entre niveles. */
public class RewardScreen
{
    public void show(ChessWorld world, int level)
    {
        int[] columns = {2, 6, 10};
        for (int i = 0; i < 3; i++) {
            world.addObject(buildCard(level), columns[i], 5);
        }
    }

    private RewardCard buildCard(int level)
    {
        int roll = Greenfoot.getRandomNumber(100);

        if (roll < 55) {
            Item it = ItemFactory.randomItem(level);
            return new RewardCard(RewardCard.KIND_ITEM, it, null, it.getName(), it.getDescription());
        }
        if (roll < 85) {
            Piece p = ItemFactory.randomRewardPiece();
            return new RewardCard(RewardCard.KIND_PIECE, null, p,
                                  "Recluta: " + p.getPieceType(),
                                  "HP " + p.getMaxHp() + " / ATQ " + p.getAttack());
        }
        return new RewardCard(RewardCard.KIND_HEAL, null, null, "Descanso",
                              "Cura por completo a todas tus piezas");
    }

    public void clear(ChessWorld world)
    {
        List<RewardCard> cards = world.getObjects(RewardCard.class);
        for (RewardCard c : cards) world.removeObject(c);
    }
}
