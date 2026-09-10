import greenfoot.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generacion procedural de cada nivel:
 *   - cantidad de enemigos: 4 y +1 cada 3 niveles
 *   - stats escalados un 10% acumulativo por nivel
 *   - posiciones aleatorias sin solapamiento dentro de la zona enemiga
 */
public class LevelGenerator
{
    public static int enemyCountForLevel(int level)
    {
        return 4 + (level - 1) / 3;
    }

    public static double statMultiplier(int level)
    {
        return 1.0 + 0.10 * (level - 1);
    }

    public static void spawnEnemies(ChessWorld world, int level)
    {
        int count = enemyCountForLevel(level);

        // Zona enemiga: 2 filas superiores del tablero (3 si hacen falta mas huecos)
        int rows = (count > ChessWorld.BOARD_SIZE * 2) ? 3 : 2;
        List<int[]> cells = new ArrayList<int[]>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < ChessWorld.BOARD_SIZE; c++) {
                cells.add(new int[]{ChessWorld.OFFSET_X + c, ChessWorld.OFFSET_Y + r});
            }
        }
        Collections.shuffle(cells);

        boolean queenUsed = false;
        double mult = statMultiplier(level);

        for (int i = 0; i < count && i < cells.size(); i++) {
            String type = pickType(level, !queenUsed);
            if ("queen".equals(type)) queenUsed = true;

            Piece enemy = createPiece(type);
            enemy.applyLevelScaling(mult);

            int x = cells.get(i)[0];
            int y = cells.get(i)[1];
            world.addObject(enemy, x, y);
            enemy.setHome(x, y);
        }
    }

    /** Ruleta ponderada: los tipos disponibles dependen del nivel. */
    private static String pickType(int level, boolean queenAllowed)
    {
        int r = Greenfoot.getRandomNumber(100);

        if (level <= 2) return "pawn";

        if (level <= 5) {
            if (r < 60) return "pawn";
            if (r < 80) return "knight";
            return "bishop";
        }

        if (level <= 8) {
            if (r < 40) return "pawn";
            if (r < 60) return "knight";
            if (r < 80) return "bishop";
            return "rook";
        }

        if (queenAllowed && r < 10) return "queen";
        if (r < 45) return "pawn";
        if (r < 62) return "knight";
        if (r < 80) return "bishop";
        return "rook";
    }

    private static Piece createPiece(String type)
    {
        if ("knight".equals(type)) return new KnightPiece(false);
        if ("bishop".equals(type)) return new BishopPiece(false);
        if ("rook".equals(type))   return new RookPiece(false);
        if ("queen".equals(type))  return new QueenPiece(false);
        return new PawnPiece(false);
    }
}
