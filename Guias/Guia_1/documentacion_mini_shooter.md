# Mini-Shooter en Greenfoot

## 1. Descripción

Se desarrolló un mini-shooter lateral en Greenfoot utilizando programación orientada a objetos y cinco patrones de diseño: **Singleton, Observer, Object Pool, Strategy y State**. La guía base propone estos patrones para organizar la gestión de puntuación, actualización del marcador, reutilización de balas, tipos de disparo y comportamiento de los enemigos. 

El jugador controla una nave, dispara a los enemigos y obtiene puntos al destruirlos. Los enemigos aparecen por la derecha y avanzan hacia la izquierda.
---

## 2. Clases principales

| Clase | Función |
|---|---|
| `Espacio` | Administra el mundo, crea los objetos y genera enemigos. |
| `Nave` | Controla el movimiento y los diferentes tipos de disparo. |
| `Bala` | Se mueve, detecta impactos y puede reutilizarse. |
| `Enemigo` | Se mueve mediante estados y puede ser destruido. |
| `GameManager` | Administra la puntuación global. |
| `Marcador` | Muestra la puntuación. |
| `PoolDeBalas` | Mantiene las balas disponibles para reutilización. |
| `BarraCarga` | Muestra visualmente la carga del disparo especial. |

---

## 3. Singleton — `GameManager`

El patrón **Singleton** permite trabajar con una única instancia de `GameManager`.

Su función principal es administrar los puntos:

```text
Enemigo destruido
       ↓
GameManager
       ↓
sumarPuntos(10)
```

El constructor de `GameManager` es privado y la instancia se obtiene mediante `getInstancia()`.

---

## 4. Observer — `Marcador`

El patrón **Observer** permite que el marcador reaccione cuando cambia la puntuación sin que el enemigo tenga que conocer directamente al marcador.

```text
GameManager
     ↓
 notificar()
     ↓
 Marcador
     ↓
actualizar()
```

`Marcador` se suscribe a `GameManager` y actualiza su imagen cuando recibe una nueva puntuación.

---

## 5. Object Pool — `PoolDeBalas`

El patrón **Object Pool** evita crear y destruir constantemente nuevas balas.

Al iniciar el juego se crea un conjunto de balas. Cuando una bala se utiliza, se activa; cuando impacta o llega al borde, se desactiva y queda disponible nuevamente.

```text
Pool
 ↓
Bala disponible
 ↓
activar()
 ↓
Bala en juego
 ↓
desactivar()
 ↓
Pool nuevamente
```

La guía utiliza este mecanismo precisamente para reutilizar las balas. 

---

## 6. Strategy — Tipos de disparo

El patrón **Strategy** permite cambiar el comportamiento del disparo sin modificar la lógica principal de la nave.

```text
EstrategiaDisparo
      │
 ┌────┼──────────┐
 ▼    ▼          ▼
Simple Triple  Cargado
```

### Disparo simple

Lanza una bala.

### Disparo triple

Lanza tres balas en diferentes ángulos.

### Disparo cargado

Es una ampliación del proyecto. El jugador mantiene presionada la tecla `SPACE` para acumular carga. Una barra visual muestra el progreso y, al alcanzar la carga mínima, al soltar la tecla se genera una bala mucho más grande y rápida.

La nave delega el comportamiento del disparo en la estrategia seleccionada, siguiendo el principio de Strategy descrito en la guía.

---

## 7. State — Movimiento del enemigo

El patrón **State** permite que el enemigo cambie su comportamiento mediante diferentes estados.

```text
Estado
  │
  ├── Avanzar
  └── Zigzag
```

El enemigo comienza en `Avanzar`. Después de cierto número de pasos cambia a `Zigzag`.

```text
Avanzar
   ↓
después de 40 pasos
   ↓
Zigzag
```

Esto evita concentrar todos los comportamientos posibles dentro de la clase `Enemigo`.
---

## 8. Game Over

Se agregaron dos condiciones de derrota:

- Si un enemigo llega al extremo izquierdo.
- Si un enemigo toca la nave.

En cualquiera de los casos, `Espacio` ejecuta:

```java
public void gameOver() {
    Greenfoot.stop();
}
```

De esta manera se detiene la ejecución del juego.

---

## 9. Disparo cargado

El disparo cargado fue agregado como una mejora del proyecto original.

Su funcionamiento es:

```text
Seleccionar modo 3
       ↓
Mantener SPACE
       ↓
Aumenta la carga
       ↓
Barra visual
       ↓
Soltar SPACE
       ↓
Disparo cargado
```

La bala cargada:

- Es considerablemente más grande que una bala normal.
- Tiene mayor velocidad.
- Puede destruir varios enemigos que encuentre en su trayectoria.
- Utiliza el mismo `PoolDeBalas`.

La `BarraCarga` se implementó como un `Actor` independiente para poder actualizarla y eliminarla correctamente del mundo cuando termina la carga o se cambia de arma.

---

## 10. Integración de los patrones

Los patrones trabajan juntos durante una partida:

```text
Nave
 ↓
Strategy
 ↓
PoolDeBalas
 ↓
Bala
 ↓
Enemigo
 ↓
GameManager
 ↓
Observer
 ↓
Marcador
```

Mientras tanto, el movimiento del enemigo es controlado por:

```text
Enemigo
 ↓
State
 ↓
Avanzar / Zigzag
```

Este flujo combina los cinco patrones en una misma experiencia de juego, tal como plantea la guía original.

---

## 11. Controles

| Tecla | Acción |
|---|---|
| `↑` | Mover nave hacia arriba |
| `↓` | Mover nave hacia abajo |
| `1` | Disparo simple |
| `2` | Disparo triple |
| `3` | Disparo cargado |
| `SPACE` | Disparar / cargar disparo especial |

---

## 12. Conclusión

El proyecto demuestra cómo los patrones de diseño pueden utilizarse para separar responsabilidades dentro de un videojuego.

Cada patrón resuelve un problema diferente:

- **Singleton:** administra un recurso global, la puntuación.
- **Observer:** actualiza el marcador sin acoplarlo al enemigo.
- **Object Pool:** reutiliza las balas.
- **Strategy:** permite intercambiar tipos de disparo.
- **State:** permite cambiar el comportamiento del enemigo.

Además, se incorporaron funcionalidades adicionales como **Game Over**, **disparo cargado**, **barra visual de carga** y **proyectiles capaces de eliminar múltiples enemigos**.
