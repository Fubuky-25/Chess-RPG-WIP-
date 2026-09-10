import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * IA enemiga. En su turno evalua el tablero y ejecuta UNA sola accion:
 *   1. Si alguna pieza puede atacar, ataca al mejor objetivo (prioriza al Rey y remates).
 *   2. Si no, mueve la pieza que mas se acerque al jugador (distancia Chebyshev).
 */
public class EnemyAI
{
    private static final int THINK_DELAY = 18;   // ticks para que el movimiento sea visible
    private int delay = 0;

    public void beginTurn() { delay = THINK_DELAY; }

    public void update(ChessWorld world)
    {
        if (delay > 0) { delay--; return; }
        playOneAction(world);
    }

    private void playOneAction(ChessWorld world)
    {
        List<Piece> enemies = world.getEnemyPieces();
        List<Piece> players = world.getPlayerPieces();

        if (enemies.isEmpty() || players.isEmpty()) {
            world.getTurnManager().endEnemyAction(world);
            return;
        }

        // ---- 1. Buscar el mejor ataque disponible ----
        Piece bestAttacker = null;
        Piece bestTarget = null;
        int bestScore = Integer.MIN_VALUE;

        for (Piece e : enemies) {
            for (Piece p : players) {
                if (e.canAttack(p.getX(), p.getY())) {
                    int score = 100;
                    if (p.isKing()) score += 50;
                    if (e.getAttack() >= p.getHp()) score += 40;   // remate
                    score += (100 - Math.min(100, p.getHp())) / 10;
                    if (score > bestScore) {
                        bestScore = score;
                        bestAttacker = e;
                        bestTarget = p;
                    }
                }
            }
        }

        if (bestAttacker != null) {
            world.startMinigame(bestAttacker, bestTarget, TimingBar.Mode.DEFENSE,
                                bestTarget.getX(), bestTarget.getY());
            return;
        }

        // ---- 2. Si nadie ataca, acercarse al jugador mas proximo ----
        List<int[]> bestMoves = new ArrayList<int[]>();
        List<Piece> bestMovers = new ArrayList<Piece>();
        int bestDistance = Integer.MAX_VALUE;

        for (Piece e : enemies) {
            for (int y = ChessWorld.OFFSET_Y; y < ChessWorld.OFFSET_Y + ChessWorld.BOARD_SIZE; y++) {
                for (int x = ChessWorld.OFFSET_X; x < ChessWorld.OFFSET_X + ChessWorld.BOARD_SIZE; x++) {
                    if (!e.canMoveTo(x, y)) continue;
                    int d = distanceToNearestPlayer(x, y, players);
                    if (d < bestDistance) {
                        bestDistance = d;
                        bestMoves.clear();
                        bestMovers.clear();
                        bestMoves.add(new int[]{x, y});
                        bestMovers.add(e);
                    } else if (d == bestDistance) {
                        bestMoves.add(new int[]{x, y});
                        bestMovers.add(e);
                    }
                }
            }
        }

        if (!bestMoves.isEmpty()) {
            int pick = Greenfoot.getRandomNumber(bestMoves.size());
            Piece mover = bestMovers.get(pick);
            int[] dest = bestMoves.get(pick);
            world.setMessage("La IA mueve su " + mover.getPieceType() + ".");
            // El deslizamiento consume el turno de la IA: el mundo pasa a
            // ANIMATING y devuelve el turno cuando la animacion termina.
            world.startSlide(mover, dest[0], dest[1], MoveFx.AFTER_ENEMY);
            return;
        }

        world.setMessage("La IA no tiene movimientos posibles y pasa.");
        world.getTurnManager().endEnemyAction(world);
    }

    /** Distancia Chebyshev: en un tablero de ajedrez la diagonal cuesta lo mismo que la recta. */
    private int distanceToNearestPlayer(int x, int y, List<Piece> players)
    {
        int best = Integer.MAX_VALUE;
        for (Piece p : players) {
            int d = Math.max(Math.abs(p.getX() - x), Math.abs(p.getY() - y));
            if (d < best) best = d;
        }
        return best;
    }
}
