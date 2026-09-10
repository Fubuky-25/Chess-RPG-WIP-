/**
 * Estados posibles del juego. Es la maquina de estados que coordina TODAS las fases:
 * ningun act() del juego hace nada si el estado actual no se lo permite.
 */
public enum GameState
{
    DEPLOY,       // Fase de despliegue: drag and drop de piezas e items
    PLAYER_TURN,  // Turno del jugador: clics tacticos
    MINIGAME,     // Barra de timing activa: todo lo demas congelado
    ANIMATING,    // Una pieza se esta desplazando: ni el jugador ni la IA actuan
    ENEMY_TURN,   // La IA ejecuta una accion
    REWARD,       // Pantalla de recompensas entre niveles
    GAME_OVER     // El Rey del jugador murio
}
