# Informe --- Dinámica DecidArch

## 1. Contexto

Como parte del primer avance del proyecto de Programación de
Videojuegos, se utilizó la dinámica DecidArch para practicar la toma de
decisiones de diseño y arquitectura considerando distintos intereses
dentro de un proyecto.

La propuesta de videojuego corresponde a un RPG táctico por turnos,
desarrollado sobre un tablero de casillas e inspirado en elementos del
ajedrez. El juego busca combinar pensamiento estratégico, habilidades de
personajes y elementos roguelike.


## 2. Carta de proyecto

El proyecto consiste en un videojuego RPG táctico por turnos en el que
los personajes se desplazan sobre un tablero de casillas. La propuesta
toma inspiración del ajedrez y agrega habilidades propias de los
personajes y elementos roguelike para aumentar la profundidad
estratégica y la rejugabilidad.


## 3. Stakeholders

Se definieron tres stakeholders principales, cada uno con objetivos y
prioridades diferentes.

### 3.1. Jugador

**Goal:** Enfrentarse a desafíos tácticos que premien la planificación,
el análisis y la toma de decisiones, incentivando la mejora del
pensamiento estratégico mediante una experiencia diferente.

**Prioridades:**

1.  **Jugabilidad --- prioridad 3:** La experiencia debe ser
    entretenida, estratégica y permitir que las decisiones del jugador
    tengan consecuencias significativas.
2.  **Balance --- prioridad 2:** Personajes, habilidades y enemigos
    deben mantenerse equilibrados para evitar estrategias dominantes y
    conservar un desafío justo.
3.  **Rendimiento --- prioridad 1:** El juego debe ejecutarse de forma
    fluida y evitar problemas que interrumpan la experiencia.

### 3.2. Desarrollador

**Goal:** Crear una versión beta funcional, estable y mantenible,
preparada para incorporar nuevas funcionalidades y contenido en futuras
iteraciones.

**Prioridades:**

1.  **Funcionalidad --- prioridad 3:** Las mecánicas principales deben
    funcionar correctamente y de forma consistente.
2.  **Optimización --- prioridad 2:** El proyecto debe utilizar los
    recursos disponibles de manera eficiente.
3.  **Escalabilidad --- prioridad 1:** La arquitectura debe facilitar la
    incorporación de personajes, habilidades, enemigos y nuevas
    mecánicas.

### 3.3. Publisher

**Goal:** Financiar y publicar un videojuego de calidad que cumpla los
plazos establecidos y tenga potencial de éxito comercial, maximizando el
retorno de la inversión sin comprometer la viabilidad del proyecto.

**Prioridades:**

1.  **Marketability --- prioridad 3:** El videojuego debe contar con
    características diferenciadoras que despierten interés en el
    mercado.
2.  **Time to Market --- prioridad 2:** El proyecto debe avanzar de
    acuerdo con los plazos establecidos.
3.  **Maintainability --- prioridad 1:** El producto debe permitir
    correcciones, actualizaciones y ampliaciones posteriores con un
    esfuerzo razonable.

## 4. Event Cards

Las Event Cards representan cambios en las condiciones del proyecto que
pueden modificar las prioridades de los stakeholders. Su objetivo es
obligar al equipo a reconsiderar decisiones anteriores.


## 5. Concern Cards

Las Concern Cards representan decisiones de diseño o arquitectura que el
equipo debe resolver. Cada preocupación presenta distintas alternativas
razonables, sin asumir que existe una solución universalmente correcta.



## 7. Conclusión


-   La propuesta inicial del videojuego.
-   Tres stakeholders y sus respectivos objetivos.
-   Prioridades de calidad para cada stakeholder.
-   Un conjunto inicial de eventos que modifican el contexto del
    proyecto.
-   Un conjunto inicial de preocupaciones con distintas alternativas de
    solución.

Este material será utilizado como referencia para las siguientes
decisiones del proyecto y podrá evolucionar a medida que aumente la
definición técnica y funcional del videojuego.
