# Proto3D — Juego de carreras (Unity)

Prototipo de un **juego de carreras estilo Mario Kart**: varias vueltas en una pista, **obstáculos en el terreno** y **rivales controlados por IA**.

> Este proyecto vive en la rama `carrera-unity`, separado del Chess RPG de `main`.

## Qué hay hoy

- Auto manejable por el jugador (flechas / WASD).
- Rivales con **IA propia** que siguen la línea de la pista, frenan en curvas y esquivan autos.
- Carrera de **3 vueltas**: cuenta regresiva, posiciones en vivo, tiempos por vuelta y pantalla de meta.
- Reaparecer en la pista con **R** (o automático si el auto vuelca o se atasca). **Enter** reinicia al terminar.

## Pendiente (la idea del juego)

- Obstáculos en el terreno y objetos tipo Mario Kart.
- Más pistas, menú, ajuste de dificultad de la IA, mejor sensación de manejo.

## Cómo abrirlo

1. Unity **2021.1.2f1** (con Unity Hub).
2. Importar la pista (no está en el repo por peso y licencia): el pack **Cartoon Tracks Pack 1** debe quedar en `Assets/CartoonTracksPack1/`. Se consigue desde *Package Manager → My Assets* con la cuenta que lo tenga.
3. Abrir la carpeta del repo como proyecto en Unity y esperar a que compile.
4. Menú **Carrera → Crear escena de carrera (sentido A)** para generar `Assets/_Game/Scenes/Race.unity` y dar Play. (Si los autos salen al revés, usar el sentido B.)

## Estructura

- `Assets/_Game/Scripts/` — lógica de la carrera: `RaceManager`, `RaceProgress`, `TrackPath`, `AIDriver`, `CarRecovery`, `CameraFollow`, `RaceHUD`, `CarPaint`.
- `Assets/_Game/Editor/RaceSetup.cs` — genera la escena, la línea de carrera y la parrilla.
- `Assets/Standard Assets/` — solo lo necesario del auto (`CarController`) de Unity Standard Assets.

## Trabajo en equipo

- No commitear `Library/`, `Obj/` ni `UserSettings/` (ya están en `.gitignore`).
- Los archivos `.meta` **sí** se suben.
- Avisen antes de editar `Race.unity` a mano: se puede regenerar desde el menú y se pierden los cambios manuales.
