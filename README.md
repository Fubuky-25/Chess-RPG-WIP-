# Proto3D — Juego de carreras (Unity)

Prototipo de un **juego de carreras estilo Mario Kart**: varias vueltas en una pista, **obstáculos en el terreno** y **rivales controlados por IA**.
Videos : https://drive.google.com/drive/folders/1JXYvYwunD5Rxlb1qeNtJcvs1VJNn_sWP?usp=sharing 
> Este proyecto vive en la rama `carrera-unity`, separado del Chess RPG de `main`.

## Qué hay hoy

- Auto manejable por el jugador (flechas / WASD).
- Rivales con **IA propia** que siguen la línea de la pista, frenan en curvas y esquivan autos.
- Carrera de **3 vueltas**: cuenta regresiva, posiciones en vivo, tiempos por vuelta y pantalla de meta.
- **Selector de color** del auto antes de cada carrera (clic o flechas, o un código hex; se recuerda el último color).
- **Rubberbanding suave**: si vas muy adelante los rivales aceleran un poco, y si vas muy atrás frenan un poco; el efecto es gradual y está limitado (ver `AIDriver`, sección *Rubberbanding*).
- Reaparecer en la pista con **R** (o automático si el auto vuelca o se atasca). **Enter** reinicia al terminar.

## Pendiente (la idea del juego)

- Obstáculos en el terreno y objetos tipo Mario Kart.
- Más pistas, menú, ajuste de dificultad de la IA, mejor sensación de manejo.

## Instalación (paso a paso)

El proyecto **no incluye la pista**: hay que descargarla una vez desde la Asset Store (es gratis) y quedará en la carpeta correcta de forma automática.

**1. Requisitos:** Unity Hub y Unity **2021.1.2f1** (otra versión puede dar errores o pedir actualizar el proyecto).

**2. Clonar la rama del juego:**

```bash
git clone -b carrera-unity https://github.com/Fubuky-25/Chess-RPG-WIP-.git Proto3D
```

**3. Descargar la pista:** [Cartoon Race Track - Oval (RCC Design, gratis)](https://marketplace.unity.com/packages/3d/environments/roadways/cartoon-race-track-oval-175061)
   1. Entrar con tu cuenta de Unity y pulsar **Add to My Assets** (se agrega a tu cuenta).
   2. Abrir el proyecto `Proto3D` en Unity Hub (la primera vez tarda varios minutos en importar).
   3. En Unity: **Window → Package Manager → Packages: My Assets**, buscar *Cartoon Race Track - Oval*, **Download** (~480 MB) e **Import**.
   4. En la ventana de importación dejar **todo marcado** y pulsar **Import**. Debe quedar la carpeta `Assets/CartoonTracksPack1/`. Esperar a que termine de compilar.

**4. Generar la escena y jugar:** menú **Carrera → Crear escena de carrera (sentido A)** y pulsar **Play**. Si los autos salen en dirección contraria a la pista, usar el sentido B. Si la consola muestra *"No encontré asfalto"*, falta importar la pista (paso 3).

### ¿Por qué la pista no está en el repositorio?

- **Peso:** las texturas de la pista (`road.psd` ~576 MB, `road_normal.png` ~175 MB) superan el límite de **100 MB por archivo** de GitHub; el repo no las acepta. Con Git LFS el cupo gratuito (1 GB) se agotaría con pocos clones del equipo.
- **Es un asset de terceros:** el repo es público y no corresponde redistribuir el trabajo de otra persona. Cada integrante lo descarga de la fuente oficial.
- **Funciona igual:** el pack de la Asset Store trae los mismos identificadores internos (`.meta`), así que la escena y los prefabs lo reconocen sin configurar nada.

## Estructura

- `Assets/_Game/Scripts/` — lógica de la carrera: `RaceManager`, `RaceProgress`, `TrackPath`, `AIDriver`, `CarRecovery`, `CameraFollow`, `RaceHUD`, `CarPaint`, `CarColorSelector`.
- `Assets/_Game/Editor/RaceSetup.cs` — genera la escena, la línea de carrera y la parrilla.
- `Assets/Standard Assets/` — solo lo necesario del auto (`CarController`) de Unity Standard Assets.

## Trabajo en equipo

- No commitear `Library/`, `Obj/` ni `UserSettings/` (ya están en `.gitignore`).
- Los archivos `.meta` **sí** se suben.
- Avisen antes de editar `Race.unity` a mano: se puede regenerar desde el menú y se pierden los cambios manuales.


