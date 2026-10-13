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
        float targetSpeed = Mathf.Min(maxSpeed, Mathf.Min(avoidSpeedCap, CornerSpeedLimit(path, d)));

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
