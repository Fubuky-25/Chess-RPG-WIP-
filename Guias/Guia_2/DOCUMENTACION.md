# Documentación — Proyecto "plataforma" (Greenfoot, Semana 4)

Juego de plataformas con los sprites de Mega Man: el personaje camina,
salta, **cae por gravedad continua** y dispara. Este documento explica qué
se implementó, por qué, y cómo se relaciona con la guía
`Semana 4 Juego Plataformas.pdf` y con la Guía 1 (mini-shooter).

## 1. Controles

| Tecla | Acción |
| --- | --- |
| ← / → | Caminar |
| ESPACIO | Saltar (solo con los pies apoyados) |
| Z | Disparar (máximo 3 balas en pantalla) |

## 2. El cambio más importante: gravedad continua

La guía (sección 10) calcula la altura del salto con la ecuación
paramétrica:

```
y(t) = y0 - v0*t + 0.5*g*t^2
```

Esa fórmula describe bien **un salto**, pero tiene una consecuencia: la
caída solo existe mientras dura ese salto. Si el personaje camina hasta el
borde de una plataforma sin saltar, se queda flotando en el aire — que era
justamente el problema a corregir.

La versión actual usa **integración por velocidad**: en vez de calcular la
posición a partir del tiempo transcurrido, se guarda una velocidad
vertical `vy` y en cada `act()` se hace:

```java
vy += GRAVEDAD;                 // la gravedad acelera hacia abajo, siempre
piesDespues = piesAntes + vy;   // y esa velocidad mueve al personaje
```

El salto ya no es un "modo especial" con su propio reloj: es simplemente
un empujón inicial hacia arriba.

```java
if (Greenfoot.isKeyDown("space") && enSuelo)
{
    vy = IMPULSO_SALTO;   // -14, negativo porque en Greenfoot Y crece hacia abajo
}
```

Las dos formas describen la misma parábola (una es la integral de la
otra). La diferencia práctica:

| | Fórmula `y(t)` de la guía | Velocidad acumulada (esta versión) |
| --- | --- | --- |
| Caer sin saltar | No ocurre | Ocurre siempre |
| Caminar fuera de una plataforma | Queda flotando | Cae |
| Caer entre plataformas | No | Sí |
| Hay que recordar cuándo empezó el salto | Sí (`tiempoSalto`) | No |

**Qué pasa si lo modificas:** subir `GRAVEDAD` hace el salto más corto y
pesado; hacer `IMPULSO_SALTO` más negativo lo hace más alto. La altura
máxima es `IMPULSO_SALTO² / (2 * GRAVEDAD)` ≈ **115 px** con los valores
actuales.

## 3. Aterrizaje: por qué no se usa `getOneIntersectingObject`

La versión anterior detectaba el aterrizaje con
`getOneIntersectingObject(Plataforma.class)`, que compara **el rectángulo
completo de la imagen**. Eso da dos problemas en este juego:

1. Los sprites tienen tamaños distintos (16x24 el idle, 26x30 el salto),
   así que la zona de colisión cambiaba según el frame de la animación.
2. El sprite de salto es ancho por los brazos abiertos: el personaje
   "chocaba" con plataformas que en pantalla ni siquiera estaba tocando.

La solución fue definir una **caja de colisión propia**, más angosta que
la imagen (`ANCHO_CAJA = 42`), y comprobar el aterrizaje a mano en
`buscarSoporte(...)`:

```
¿La caja del jugador se superpone horizontalmente con la plataforma?
        y
¿La superficie de la plataforma queda dentro del tramo que recorren
 los pies en este act()?   ->   entonces el jugador se apoya ahí.
```

Revisar **el tramo recorrido** y no solo la posición final es lo que evita
que a alta velocidad de caída el personaje atraviese una plataforma
delgada (el clásico bug de "tunneling"): en un `act()` puede moverse hasta
16 px, y las plataformas miden 20 px de alto.

La búsqueda se hace en cada `act()`, incluso estando quieto. Por eso
caminar fuera de una plataforma funciona solo: deja de haber superposición
horizontal, no se encuentra soporte, `enSuelo` queda en `false` y la
gravedad hace el resto.

## 4. Sprites: por qué todos se dibujan en un lienzo del mismo tamaño

Los archivos de `images/` no miden lo mismo:

| Archivo | Tamaño | Uso |
| --- | --- | --- |
| `idle.png` | 16x24 | Quieto |
| `walk1..4.png` | 16-24 x 22-24 | Caminata |
| `jump.png` | 26x30 | Salto y caída |

Greenfoot ubica un actor **por el centro de su imagen**. Si cada frame
tiene un alto distinto, al animar la caminata el personaje sube y baja
solo, y los pies se hunden en el piso.

`prepararSprite(...)` resuelve esto: escala el sprite x3 y lo pega
**centrado y apoyado al fondo** de un lienzo fijo de 78x90 px (el tamaño
del sprite más grande). Así los pies quedan siempre en el borde inferior
de la imagen, y `pies() = getY() + ALTO_IMAGEN / 2` vale para cualquier
frame.

Las versiones espejadas se calculan **una sola vez** al cargar, no en cada
`act()` como antes: crear una imagen nueva 50 veces por segundo es trabajo
tirado a la basura.

`jump.png` se usa tanto para subir como para caer, igual que en el Mega
Man original.

## 5. Disparo (idea tomada de la Guía 1 / mini-shooter)

`Bala` es un `Actor` con una sola responsabilidad: avanzar en línea recta
y eliminarse al llegar al borde. No sabe quién la disparó ni contra qué
choca, así que agregar enemigos más adelante no obliga a tocar `Jugador`.

Dos detalles tomados del Mega Man original:

- **Una bala por pulsación**: `Jugador` recuerda si `Z` ya estaba
  presionada (`teclaDisparoAntes`), así mantenerla apretada no dispara en
  ráfaga.
- **Máximo 3 balas en pantalla** (`MAXIMO_BALAS`), contadas con
  `getWorld().getObjects(Bala.class).size()`.

Todavía **no** se usa el `PoolDeBalas` de la Guía 1. Con 3 balas
simultáneas el Object Pool no aporta nada real, y el `CLAUDE.md` pide
priorizar la solución simple. Es una extensión natural si más adelante
hay muchos proyectiles en pantalla.

## 6. Nivel

El suelo es **una `Plataforma` más** (ancha, al fondo), no un caso
especial: así el aterrizaje usa el mismo código en el piso y en el aire.

| Elemento | Centro (x, y) | Tamaño | Superficie (y) |
| --- | --- | --- | --- |
| Suelo | (400, 470) | 800x60 | 440 |
| Plataforma 1 | (180, 370) | 150x20 | 360 |
| Plataforma 2 | (390, 300) | 150x20 | 290 |
| Plataforma 3 | (300, 210) | 100x20 | 200 |
| Plataforma 4 | (600, 235) | 140x20 | 225 |
| Plataforma 5 | (740, 160) | 120x20 | 150 |

Los saltos suben 115 px y avanzan hasta 170 px en horizontal. Cada escalón
del nivel sube entre 65 y 90 px y deja huecos de 60-65 px: todo es
alcanzable con margen, y los huecos son lo bastante anchos como para
caerse por ellos (que es la mecánica pedida).

## 7. Recursos pendientes

Dos imágenes están dibujadas por código a propósito, para que el proyecto
abra siempre aunque falten archivos. Cuando lleguen los sprites, cada
cambio es **una línea**, ya marcada con `<<< CAMBIAR AQUI` en el código:

| Archivo esperado | Dónde se cambia | Qué se usa mientras tanto |
| --- | --- | --- |
| `images/bala.png` | `Bala.java`, constructor | Óvalo celeste dibujado por código |
| `images/plataforma.png` | `Plataforma.java`, constructor | Bloque azul dibujado por código |
| `images/shoot.png` (opcional) | `Jugador.java`, animación | Se muestra el sprite normal |

`Plataforma.crearImagenConTile()` ya está escrito: repite un tile cuadrado
hasta cubrir el ancho y alto pedidos, así una imagen chica sirve para
plataformas de cualquier largo.

## 8. Limitaciones conocidas (a propósito, por simplicidad)

- **Las plataformas se atraviesan desde abajo.** Solo se detecta el
  aterrizaje (caída), no el golpe de cabeza. Se puede saltar a través de
  una plataforma y caer sobre ella.
- **No hay colisión lateral.** Caminando contra el costado de una
  plataforma alta, el personaje se superpone a ella. Con plataformas de 20
  px de alto casi no se nota.
- **El disparo no tiene sprite propio.** Al disparar se sigue viendo el
  sprite de quieto o de caminata.
- **No hay condición de victoria ni derrota.** La mecánica pedida era
  moverse, saltar entre plataformas y disparar.

## 9. Actividad de cierre (sección 16 de la guía)

| Driver o concern | Decisión tomada | Elemento de código |
| --- | --- | --- |
| Movimiento natural | Gravedad continua por velocidad acumulada, en vez de una fórmula de posición válida solo durante el salto | `aplicarGravedad()`, campo `vy` |
| Precisión de la física | La posición vertical se guarda en un `double` aparte, porque `getY()` solo guarda enteros y redondear cada `act()` acumula error | campo `yReal`, `ubicarPies(...)` |
| Robustez | El aterrizaje revisa el tramo recorrido, no solo la posición final, para no atravesar plataformas delgadas al caer rápido | `buscarSoporte(piesAntes, piesDespues)` |
| Consistencia visual | Todos los frames se dibujan en un lienzo del mismo tamaño, apoyados al fondo | `prepararSprite(...)` |
| Rendimiento | Los sprites escalados y espejados se calculan una vez al cargar, no en cada `act()` | `cargarImagenes()`, `espejo(...)` |
| Mantenibilidad | Cada responsabilidad en su propio método; estados con `enum` en vez de enteros | `enum Estado`, `controlarMovimientoHorizontal()`, `controlarSalto()`, `aplicarGravedad()`, `actualizarEstado()`, `actualizarAnimacion()` |
| Separación de responsabilidades | `Plataforma` no tiene `act()`: la colisión la resuelve solo `Jugador`, en un único lugar | `Plataforma.java`, `buscarSoporte(...)` |
| Extensibilidad | El suelo es una `Plataforma` más, y `Bala` no conoce a nadie: agregar enemigos no obliga a tocar `Jugador` | `Mundo.crearSuelo()`, `Bala.java` |

## 10. Experimentos sugeridos

1. Cambiar `GRAVEDAD` de `0.8` a `1.5`: el salto se siente pesado y corto.
2. Cambiar `IMPULSO_SALTO` de `-14` a `-18` y ver qué plataformas quedan
   demasiado fáciles.
3. Bajar `CAIDA_MAXIMA` de `16` a `4`: el personaje "flota" al caer.
4. Subir `CAIDA_MAXIMA` a `40` y comprobar que igual **no** atraviesa las
   plataformas (ahí se nota para qué sirve revisar el tramo recorrido).
5. Cambiar `MAXIMO_BALAS` de `3` a `1` o a `10`.

## 11. Próximos pasos posibles

- Integrar `bala.png` y el tile de plataforma.
- Sprite de disparo (`shoot.png`) y estado `DISPARANDO` en la animación.
- Enemigos simples que patrullen una plataforma y mueran con el disparo
  (ahí sí tendría sentido reutilizar `PoolDeBalas` y el patrón State de la
  Guía 1).
- Colisión lateral y golpe de cabeza contra las plataformas.
- Sonidos de salto y disparo (la carpeta `sounds/` ya existe y está vacía).
