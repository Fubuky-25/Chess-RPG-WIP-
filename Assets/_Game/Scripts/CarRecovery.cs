
using System;
using System.Collections;
using UnityEngine;
using UnityStandardAssets.Vehicles.Car;

// Controla el respawn y las explosiones por choques fuertes.
[RequireComponent(typeof(RaceProgress))]
public class CarRecovery : MonoBehaviour
{
    [Header("Reaparicion")]
    public KeyCode manualKey = KeyCode.R;
    public bool allowManual;
    public float flippedSeconds = 2.5f;
    public float fallY = -20f;

    [Header("Explosion por choque")]
    public bool activarExplosion = true;
    public bool soloJugador = true;
    public float velocidadMinimaKmh = 120f;
    public float tiempoReaparicion = 1f;

    [Header("Identificacion de barandas")]
    public string etiquetaBaranda = "Baranda";
    public string[] nombresBaranda;

    public bool mostrarDiagnostico = true;

    private RaceProgress progress;
    private Rigidbody body;
    private float flippedTimer;
    private bool enExplosion = false;

    // ====================================
    // INICIALIZACION
    // ====================================

    void Awake()
    {
        progress = GetComponent<RaceProgress>();
        body = GetComponent<Rigidbody>();

        // Todos los colliders detectados en la pista.
        // Se cargan al iniciar, incluso si el Inspector
        // conserva una lista anterior.
        nombresBaranda = new string[]
        {
            "baranda",
            "guardrail",
            "guard_rail",
            "railing",
            "barrier",
            "fence",
            "wall",
            "valla",
            "muro",

            "collider_001",
            "collider_002",
            "collider_003",
            "collider_004",
            "collider_005",
            "collider_006",
            "collider_007",
            "collider_008",
            "collider_09",
            "collider_010",
            "collider_011",
            "collider_012",
            "collider_013",
            "collider_014",
            "collider_015",
            "collider_016",
            "collider_017",
            "collider_018",
            "collider_019",
            "collider_020",

            "collider_garage_floor",
            "collider_garages_001",
            "collider_garages_002",
            "collider_garages_003",
            "collider_garages_004",
            "collider_garages_005",
            "collider_garages_006",
            "collider_garages_007",
            "collider_garages_008",
            "collider_garages_009",
            "collider_garages_010",
            "collider_garages_011",
            "collider_garages_012",
            "collider_garages_013",
            "collider_garages_014",
            "collider_garages_015",
            "collider_garages_016",
            "collider_garages_017",
            "collider_garages_018",
            "collider_garages_019",
            "collider_garages_020",

            "0GRASS2",
            "1CONCRETE_red",
            "1GRASS",
            "1GRASS_2",
            "1GRAVEL",
            "1PITLANE",
            "1TARMAC_back",
            "1TARMAC_inner",
            "1TARMAC_oval",
            "1TARMACgarages",
            "Cylinder001",
            "kerbs",
            "Object021",
            "sand_outside"
        };
    }

    // ====================================
    // ACTUALIZACION
    // ====================================

    void Update()
    {
        if (enExplosion) return;

        if (allowManual && Input.GetKeyDown(manualKey))
        {
            Respawn();
        }
    }

    void FixedUpdate()
    {
        if (enExplosion) return;

        if (transform.position.y < fallY)
        {
            Respawn();
            return;
        }

        if (transform.up.y < 0.3f)
        {
            flippedTimer += Time.fixedDeltaTime;
        }
        else
        {
            flippedTimer = 0f;
        }

        if (flippedTimer > flippedSeconds)
        {
            Respawn();
        }
    }

    // ====================================
    // DETECTAR COLISIONES
    // ====================================

    void OnCollisionEnter(Collision collision)
    {
        if (!activarExplosion || enExplosion) return;

        if (soloJugador && !progress.isPlayer) return;

        if (progress.path == null) return;

        if (RaceManager.Instance != null &&
            RaceManager.Instance.State != RaceManager.RaceState.Racing)
        {
            return;
        }

        // Calcular la velocidad en km/h.
        float velocidadImpacto = Mathf.Max(
            body.velocity.magnitude,
            collision.relativeVelocity.magnitude
        ) * 3.6f;

        // Solo impactos que superen 120 km/h.
        if (velocidadImpacto <= velocidadMinimaKmh)
        {
            return;
        }

        // Comprobar que el objeto esté en la lista.
        bool esBaranda = EsBaranda(collision.collider.transform);

        // Buscar un contacto principalmente lateral.
        bool impactoLateral = false;
        Vector3 puntoImpacto = transform.position;

        for (int i = 0; i < collision.contactCount; i++)
        {
            ContactPoint contacto = collision.GetContact(i);

            // Una normal vertical suele indicar suelo.
            // Una normal horizontal indica pared lateral.
            float componenteVertical =
                Mathf.Abs(Vector3.Dot(
                    contacto.normal,
                    Vector3.up
                ));

            if (componenteVertical < 0.65f)
            {
                impactoLateral = true;
                puntoImpacto = contacto.point;
                break;
            }
        }

        // Mensajes para comprobar el funcionamiento.
        if (mostrarDiagnostico)
        {
            Debug.Log(
                "[Choque] Objeto: " + collision.collider.name +
                " | Velocidad: " + velocidadImpacto.ToString("F1") +
                " km/h | Baranda: " + esBaranda +
                " | Lateral: " + impactoLateral
            );
        }

        if (!esBaranda || !impactoLateral)
        {
            return;
        }

        // Activar explosion y reaparicion.
        StartCoroutine(
            ExplotarYReaparecer(puntoImpacto)
        );
    }

    // ====================================
    // IDENTIFICAR OBJETOS
    // ====================================

    bool EsBaranda(Transform objeto)
    {
        Transform actual = objeto;

        while (actual != null)
        {
            if (actual.gameObject.tag == etiquetaBaranda)
            {
                return true;
            }

            string nombre = actual.name;

            foreach (string palabra in nombresBaranda)
            {
                if (string.IsNullOrEmpty(palabra))
                {
                    continue;
                }

                // Coincidencia exacta para colliders
                // de la pista.
                if (string.Equals(
                    nombre,
                    palabra,
                    StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }

                // Reconocer nombres descriptivos
                // como wall, barrier, guardrail, etc.
                if (!palabra.StartsWith("collider_",
                        StringComparison.OrdinalIgnoreCase)
                    && !palabra.StartsWith("1")
                    && !palabra.StartsWith("0")
                    && nombre.IndexOf(
                        palabra,
                        StringComparison.OrdinalIgnoreCase) >= 0)
                {
                    return true;
                }
            }

            actual = actual.parent;
        }

        return false;
    }

    // ====================================
    // EXPLOSION Y RESPAWN
    // ====================================

    IEnumerator ExplotarYReaparecer(Vector3 puntoImpacto)
    {
        enExplosion = true;

        // Desactivar controles temporalmente.
        CarUserControl jugador =
            GetComponent<CarUserControl>();

        AIDriver ia =
            GetComponent<AIDriver>();

        bool jugadorActivo =
            jugador != null && jugador.enabled;

        bool iaActiva =
            ia != null && ia.enabled;

        if (jugador != null)
            jugador.enabled = false;

        if (ia != null)
            ia.enabled = false;

        // Detener movimiento del auto.
        bool eraKinematic = body.isKinematic;

        body.velocity = Vector3.zero;
        body.angularVelocity = Vector3.zero;
        body.isKinematic = true;

        // Ocultar visualmente el auto.
        Renderer[] renderers =
            GetComponentsInChildren<Renderer>();

        bool[] visibles =
            new bool[renderers.Length];

        for (int i = 0; i < renderers.Length; i++)
        {
            visibles[i] = renderers[i].enabled;
            renderers[i].enabled = false;
        }

        // Crear explosion.
        CrearExplosion(puntoImpacto + Vector3.up * 0.8f);

        if (mostrarDiagnostico)
        {
            Debug.Log(
                "[Explosion] Impacto fuerte detectado. " +
                "Reaparicion en " + tiempoReaparicion +
                " segundos."
            );
        }

        // Esperar antes de reaparecer.
        yield return new WaitForSeconds(
            tiempoReaparicion
        );

        // Restaurar fisica.
        body.isKinematic = eraKinematic;

        // Utilizar el respawn original.
        Respawn();

        // Mostrar nuevamente el auto.
        for (int i = 0; i < renderers.Length; i++)
        {
            if (renderers[i] != null)
            {
                renderers[i].enabled = visibles[i];
            }
        }

        // Restaurar controles.
        if (jugador != null)
            jugador.enabled = jugadorActivo;

        if (ia != null)
            ia.enabled = iaActiva;

        enExplosion = false;
    }

    // ====================================
    // EFECTO VISUAL DE EXPLOSION
    // ====================================

    void CrearExplosion(Vector3 posicion)
    {
        GameObject explosion =
            new GameObject("ExplosionAuto");

        explosion.transform.position = posicion;

        ParticleSystem particulas =
            explosion.AddComponent<ParticleSystem>();

        particulas.Stop(
            true,
            ParticleSystemStopBehavior.StopEmittingAndClear
        );

        var main = particulas.main;

        main.duration = 0.4f;
        main.loop = false;
        main.startLifetime = 0.7f;
        main.startSpeed = 8f;
        main.startSize = 1.2f;
        main.maxParticles = 80;
        main.startColor =
            new Color(1f, 0.45f, 0f, 1f);

        var emission = particulas.emission;

        emission.rateOverTime = 0f;

        emission.SetBursts(
            new ParticleSystem.Burst[]
            {
                new ParticleSystem.Burst(0f, 60)
            }
        );

        var shape = particulas.shape;

        shape.shapeType =
            ParticleSystemShapeType.Sphere;

        shape.radius = 0.8f;

        particulas.Play();

        // Eliminar efecto despues de reproducirlo.
        Destroy(explosion, 2f);
    }

    // ====================================
    // RESPAWN
    // ====================================

    public void Respawn()
    {
        TrackPath path = progress.path;

        if (path == null) return;

        flippedTimer = 0f;

        float d = progress.PathDistance;

        Vector3 pos = path.GetPoint(d);
        Vector3 fwd = path.GetTangent(d);

        // Apoyar el auto sobre el asfalto.
        RaycastHit hit;

        if (Physics.Raycast(
            pos + Vector3.up * 20f,
            Vector3.down,
            out hit,
            60f))
        {
            pos.y = hit.point.y;
        }

        pos.y += 1f;

        body.velocity = Vector3.zero;
        body.angularVelocity = Vector3.zero;

        transform.SetPositionAndRotation(
            pos,
            Quaternion.LookRotation(
                fwd,
                Vector3.up
            )
        );

        // Liberar frenos residuales.
        WheelCollider[] ruedas =
            GetComponentsInChildren<WheelCollider>();

        foreach (WheelCollider rueda in ruedas)
        {
            rueda.brakeTorque = 0f;
        }
    }
}
