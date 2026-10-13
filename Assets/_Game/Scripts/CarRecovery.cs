using UnityEngine;

// Devuelve el auto a la pista si queda volcado, cae fuera del mapa o el jugador pulsa R.
[RequireComponent(typeof(RaceProgress))]
public class CarRecovery : MonoBehaviour
{
    public KeyCode manualKey = KeyCode.R;
    public bool allowManual;          // solo el jugador
    public float flippedSeconds = 2.5f;
    public float fallY = -20f;

    RaceProgress progress;
    Rigidbody body;
    float flippedTimer;

    void Awake()
    {
        progress = GetComponent<RaceProgress>();
        body = GetComponent<Rigidbody>();
    }

    void Update()
    {
        if (allowManual && Input.GetKeyDown(manualKey)) Respawn();
    }

    void FixedUpdate()
    {
        if (transform.position.y < fallY) { Respawn(); return; }

        if (transform.up.y < 0.3f) flippedTimer += Time.fixedDeltaTime; else flippedTimer = 0f;
        if (flippedTimer > flippedSeconds) Respawn();
    }

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
        if (Physics.Raycast(pos + Vector3.up * 20f, Vector3.down, out hit, 60f)) pos.y = hit.point.y;
        pos.y += 1f;

        body.velocity = Vector3.zero;
        body.angularVelocity = Vector3.zero;
        transform.SetPositionAndRotation(pos, Quaternion.LookRotation(fwd, Vector3.up));
    }
}
