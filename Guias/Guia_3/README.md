# Roguelike de oleadas (Greenfoot)

Un juego de supervivencia con vista cenital, hecho para el laboratorio de la semana 5.
Estás encerrado en una habitación de piedra y no paran de entrar monstruos por los bordes.
Aguantas lo que puedas. La estética es un homenaje directo a The Binding of Isaac.

## Cómo se juega

Se mueve con WASD.

Para disparar hay dos opciones:

- Las flechas disparan en esa dirección. Puedes caminar hacia un lado y disparar hacia el otro,
  como en el Isaac original. La cabeza del personaje gira hacia donde estás apuntando.
- La barra espaciadora dispara hacia donde vas caminando. Es más cómodo si prefieres una sola mano.

Empiezas con 100 de vida. Cada monstruo que te toca te quita un mordisco y después hay medio segundo
de invulnerabilidad, así que quedar rodeado no te mata al instante, pero tampoco te da mucho margen.
Cuando la vida llega a cero aparece la pantalla de game over con la oleada a la que llegaste.

Arriba a la izquierda está el HUD: número de oleada, enemigos vivos, balas en pantalla y la barra roja de vida.

## Las oleadas

Cada oleada tiene un total de enemigos y un máximo de enemigos vivos al mismo tiempo. No es lo mismo.
La oleada 10 manda unos 35 monstruos, pero nunca hay más de 26 en la sala: van entrando a medida que los matas.
Eso es a propósito. Si los dejáramos crecer sin control, el juego se pondría lento antes de ponerse difícil.

Cuando limpias una oleada aparece el cartel de la siguiente y tienes unos segundos de respiro.
La dificultad sube por varios lados a la vez: más enemigos, más vida, más velocidad, más daño y
aparición más seguida. Además cada oleada elige al azar una de cuatro variantes (normal, muchos y
débiles, pocos y duros, o aparición acelerada), así que dos partidas no se sienten iguales.

## Cómo está armado el código

| Clase | De qué se encarga |
| --- | --- |
| `JuegoWorld` | La sala, el fondo, los límites de los muros y el game over |
| `Soldado` | Clase abstracta con lo que comparten jugador y enemigo: vida, velocidad, recibir daño y morir |
| `Jugador` | Teclado, movimiento, disparo y qué sprite mostrar en cada momento |
| `SoldadoEnemigo` | Persecución, ataque cuerpo a cuerpo y su animación de dos frames |
| `Bala` | Se mueve, revienta al tocar un enemigo o un muro y se borra sola a los 80 ciclos |
| `GestorOleadas` | Cuántos enemigos crear, cada cuánto y con qué parámetros |
| `HUD` | El cartel de arriba, que se redibuja cada 10 ciclos y no en cada frame |
| `FabricaImagenes` | Carga los sprites una sola vez y arma las combinaciones del personaje |

El enemigo no sabe en qué oleada está. Solo recibe su vida, velocidad y daño cuando nace.
Toda la lógica de dificultad vive en `GestorOleadas`, así que se puede reequilibrar el juego
sin tocar el comportamiento del monstruo.

## El personaje se arma con dos piezas

Los sprites vienen separados: por un lado la cabeza en cuatro direcciones, por otro las piernas
caminando. `FabricaImagenes` los pega en un lienzo fijo de 28x36 píxeles con los pies siempre
apoyados en el borde inferior, y después escala todo al doble. Por eso el personaje puede correr
hacia la derecha mirando hacia arriba.

Combinaciones posibles hay 120 (cuatro cabezas por treinta poses de cuerpo, contando el espejado
para caminar a la izquierda). Cada una se arma la primera vez que hace falta y después queda
guardada en un arreglo. Durante la partida `act()` no crea imágenes nuevas, solo pide la que ya existe.

## Los límites que respeta el juego

- Máximo 35 enemigos simultáneos
- Máximo 60 balas en pantalla
- Cada bala vive 80 ciclos como mucho
- El HUD se redibuja cada 10 ciclos

Con esos topes la cantidad de actores se queda cerca del centenar en el peor caso, en vez de crecer
hasta que el juego se arrastre. Si quieres ver por qué importan, sube el máximo simultáneo a 500
y prueba a moverte.

## Recursos

Los sprites del personaje, el monstruo y el fondo están en `images/`.
La bala todavía se dibuja por código: es la lágrima celeste que arma `crearLagrima()` en
`FabricaImagenes`, y esa línea está marcada para reemplazarla en cuanto tengas un sprite.

El fondo original es 16:9 y la ventana del juego es 900x600, así que se escala a lo ancho sin
deformar y el alto que sobra se rellena repitiendo piso. El muro de piedra no es decoración:
el jugador, los monstruos y las balas se quedan dentro del suelo.

## Para abrirlo

Abre la carpeta `Roguelike` con Greenfoot, compila y presiona Run.
