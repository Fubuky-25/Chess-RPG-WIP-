using UnityEngine;
using UnityStandardAssets.Vehicles.Car;

// Piloto automatico propio: sigue la linea de carrera (TrackPath), frena antes de las curvas
// y esquiva a otros autos. Alimenta el mismo CarController que usa el jugador.
[RequireComponent(typeof(RaceProgress))]
public class AIDriver : MonoBehaviour
{
    [Header("Habilidad")]
    [Tooltip("Velocidad maxima de este piloto en m/s (30 = 108 km/h).")]
    public float maxSpeed = 40f;
    [Tooltip("Aceleracion lateral que se atreve a usar en curva (m/s2). Mas alto = curvas mas rapidas.")]
    public float cornerGrip = 11f;
    [Tooltip("Desaceleracion que asume al frenar (m/s2).")]
    public float brakeDecel = 14f;
    [Tooltip("Posicion lateral preferida respecto al centro de la pista (m, + derecha).")]
    public float preferredOffset = 0f;

    [Header("Rubberbanding (ajuste dinamico de dificultad)")]
    [Tooltip("Si esta activo, el rival acelera cuando el jugador va muy adelante y frena cuando el jugador va muy atras.")]
    public bool rubberBanding = true;
    [Tooltip("Diferencia de distancia (m) bajo la cual no se hace nada: carrera 'normal'.")]
    public float rubberDeadZone = 25f;
    [Tooltip("Diferencia de distancia (m) a partir de la cual se aplica el efecto maximo.")]
    public float rubberFullGap = 140f;
    [Tooltip("Aumento maximo de velocidad cuando el jugador va adelante (0.10 = +10%).")]
    [Range(0f, 0.3f)] public float rubberMaxBoost = 0.10f;
    [Tooltip("Reduccion maxima de velocidad cuando el jugador va atras (0.12 = -12%).")]
    [Range(0f, 0.3f)] public float rubberMaxSlow = 0.12f;
    [Tooltip("Que tan rapido puede cambiar el multiplicador (0.03 = 3% por segundo). Mas bajo = menos evidente.")]
    public float rubberChangeRate = 0.03f;

    [Header("Conduccion")]
    public float maxSteerAngle = 25f;
    public float lookAheadMin = 7f;
    public float lookAheadPerSpeed = 0.35f;

    [HideInInspector] public bool canDrive;

    RaceProgress progress;
    CarController car;
    Rigidbody body;
    CarRecovery recovery;

    float offset;          // desvio lateral actual de la linea objetivo
    float stuckTimer;
    float speedMultiplier = 1f;   // factor de rubberbanding actual (1 = sin efecto)

    // Para depurar: factor de velocidad que el rubberbanding aplica ahora mismo.
    public float SpeedMultiplier { get { return speedMultiplier; } }

    void Awake()
    {
        progress = GetComponent<RaceProgress>();
        car = GetComponent<CarController>();
        body = GetComponent<Rigidbody>();
        recovery = GetComponent<CarRecovery>();
        offset = preferredOffset;
    }

    void FixedUpdate()
    {
        TrackPath path = progress.path;
        if (!canDrive || path == null) return;

        float dt = Time.fixedDeltaTime;
        float d = progress.PathDistance;
        float speed = body.velocity.magnitude;

        // 1) Desvio lateral objetivo: el preferido, o esquivando a un auto cercano.
        float avoidSpeedCap = float.MaxValue;
        float wantedOffset = AvoidOffset(path, d, speed, out avoidSpeedCap);
        float limit = Mathf.Max(0f, path.halfWidth - 2.5f);
        wantedOffset = Mathf.Clamp(wantedOffset, -limit, limit);
        offset = Mathf.MoveTowards(offset, wantedOffset, 3f * dt);

        // 2) Punto al que apuntamos.
        float look = lookAheadMin + speed * lookAheadPerSpeed;
        float dl = d + look;
        Vector3 target = path.GetPoint(dl) + path.GetRight(dl) * offset;
        Vector3 local = transform.InverseTransformPoint(target);
        float angle = Mathf.Atan2(local.x, local.z) * Mathf.Rad2Deg;
        float steer = Mathf.Clamp(angle / maxSteerAngle, -1f, 1f);

        // 3) Velocidad objetivo segun las curvas que vienen.
        //    El rubberbanding escala el techo de velocidad; en curva los aumentos cuentan solo a medias
        //    para no pasarse del agarre, las reducciones si cuentan completas.
        UpdateRubberBand(dt);
        float cornerMult = speedMultiplier > 1f ? 1f + (speedMultiplier - 1f) * 0.5f : speedMultiplier;
        float targetSpeed = Mathf.Min(maxSpeed * speedMultiplier,
                            Mathf.Min(avoidSpeedCap, CornerSpeedLimit(path, d) * cornerMult));

        float err = targetSpeed - speed;
        float accel = Mathf.Clamp01(err * 0.5f);
        float brake = err < -1.5f ? Mathf.Clamp01(-err * 0.35f) : 0f;
        if (Mathf.Abs(steer) > 0.7f && speed > 12f) accel *= 0.6f;

        // 4) Atascado (choque contra un muro, por ejemplo): volver a la pista.
        if (speed < 1.5f && targetSpeed > 5f) stuckTimer += dt; else stuckTimer = 0f;
        if (stuckTimer > 3f)
        {
            stuckTimer = 0f;
            if (recovery != null) recovery.Respawn();
        }

        car.Move(steer, accel, -brake, 0f);
    }

    // Rubberbanding: compara la distancia recorrida por el jugador con la de este rival y calcula un
    // multiplicador de velocidad. El multiplicador deseado crece de forma suave con la diferencia
    // (zona muerta + smoothstep) y el multiplicador real solo puede cambiar a rubberChangeRate por
    // segundo, asi el cambio de ritmo es gradual y dificil de notar.
    void UpdateRubberBand(float dt)
    {
        float wanted = 1f;
        RaceManager rm = RaceManager.Instance;
        if (rubberBanding && rm != null && rm.Player != null && rm.State == RaceManager.RaceState.Racing
            && !rm.Player.Finished && !progress.Finished && rubberFullGap > rubberDeadZone)
        {
            // Positivo: el jugador va por delante de este rival. Negativo: va por detras.
            float gap = rm.Player.TotalDistance - progress.TotalDistance;
            float t = Mathf.Clamp01((Mathf.Abs(gap) - rubberDeadZone) / (rubberFullGap - rubberDeadZone));
            t = t * t * (3f - 2f * t); // smoothstep
            wanted = gap > 0f ? 1f + rubberMaxBoost * t : 1f - rubberMaxSlow * t;
        }
        speedMultiplier = Mathf.MoveTowards(speedMultiplier, wanted, rubberChangeRate * dt);
    }

    // Revisa curvas hasta ~90 m adelante y devuelve la velocidad a la que hay que ir ahora
    // para poder llegar a cada una con la velocidad que permite su radio.
    float CornerSpeedLimit(TrackPath path, float d)
    {
        float best = float.MaxValue;
        for (float k = 10f; k <= 90f; k += 10f)
        {
            Vector3 a = path.GetTangent(d + k);
            Vector3 b = path.GetTangent(d + k + 8f);
            float turn = Vector3.Angle(a, b) * Mathf.Deg2Rad;     // radianes en 8 m
            float curvature = turn / 8f;
            float vCorner = curvature > 0.0005f ? Mathf.Sqrt(cornerGrip / curvature) : float.MaxValue;
            float vNow = Mathf.Sqrt(vCorner * vCorner + 2f * brakeDecel * k);
            if (vNow < best) best = vNow;
        }
        return best;
    }

    // Si hay un auto delante en nuestra trayectoria, elige pasar por un lado y limita la velocidad.
    float AvoidOffset(TrackPath path, float d, float speed, out float speedCap)
    {
        speedCap = float.MaxValue;
        float wanted = preferredOffset;
        if (RaceManager.Instance == null) return wanted;

        float closest = float.MaxValue;
        foreach (RaceProgress other in RaceManager.Instance.Racers)
        {
            if (other == null || other == progress) continue;
            float ahead = path.Delta(d, other.PathDistance);
            if (ahead < 0f || ahead > 22f) continue;
            float side = other.Lateral - progress.Lateral;
            if (Mathf.Abs(side) > 3.2f) continue;
            if (ahead < closest)
            {
                closest = ahead;
                // Pasar por el lado con mas espacio.
                float dir = side > 0f ? -1f : 1f;
                if (Mathf.Abs(side) < 0.3f) dir = other.Lateral > 0f ? -1f : 1f;
                wanted = other.Lateral + dir * 3.4f;
                if (ahead < 9f)
                {
                    float otherSpeed = other.body != null ? other.body.velocity.magnitude : speed;
                    speedCap = Mathf.Max(5f, otherSpeed - 1f + ahead * 0.5f);
                }
            }
        }
        return wanted;
    }
}
