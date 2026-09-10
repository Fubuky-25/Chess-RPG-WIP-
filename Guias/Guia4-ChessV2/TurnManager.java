/**
 * Control de estados y alternancia de turnos.
 * Turnos ALTERNADOS: 1 accion del jugador -> 1 accion de la IA -> repetir.
 */
public class TurnManager
{
    private GameState state = GameState.DEPLOY;

    public GameState getState()          { return state; }
    public void setState(GameState s)    { state = s; }

    /** Llamado cuando el jugador termino su unica accion del turno. */
    public void endPlayerAction(ChessWorld world)
    {
        if (world.getEnemyPieces().isEmpty()) {
            world.beginReward();
            return;
        }
        state = GameState.ENEMY_TURN;
        world.getEnemyAI().beginTurn();
    }

    /** Llamado cuando la IA termino su unica accion del turno. */
    public void endEnemyAction(ChessWorld world)
    {
        if (world.getPlayerPieces().isEmpty()) {
            world.triggerGameOver();
            return;
        }
        if (world.getEnemyPieces().isEmpty()) {
            world.beginReward();
            return;
        }
        state = GameState.PLAYER_TURN;
    }
}
