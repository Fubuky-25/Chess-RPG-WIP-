PROYECTO: plataforma (Greenfoot) — Semana 4
-------------------------------------------

Juego de plataformas con los sprites de Mega Man. El personaje camina,
salta entre plataformas, cae por gravedad continua y dispara.

CONTROLES
  Flecha izquierda / derecha .... caminar
  ESPACIO ....................... saltar (solo con los pies en el suelo)
  Z ............................. disparar (maximo 3 balas en pantalla)

CLASES
  Mundo ........ arma el nivel: suelo, plataformas y jugador.
  Jugador ...... movimiento, gravedad, salto, animacion y disparo.
  Plataforma ... superficie solida donde el jugador se apoya.
  Bala ......... proyectil que avanza recto y se elimina en el borde.

COMO EJECUTAR
  1. Greenfoot -> Open Project -> carpeta "plataforma".
  2. Compile (hay clases nuevas por compilar).
  3. Run.

PENDIENTE (faltan archivos de imagen)
  images/bala.png ......... sprite del disparo. Mientras no exista, la
                            bala se dibuja por codigo. Al agregarlo,
                            cambiar la linea marcada en Bala.java.
  images/plataforma.png ... tile de la plataforma. Mientras no exista, el
                            bloque se dibuja por codigo. Al agregarlo,
                            cambiar la linea marcada en Plataforma.java.
  images/shoot.png ........ pose disparando (opcional).

Ver DOCUMENTACION.md para el detalle de la fisica y de las decisiones de
diseno.
