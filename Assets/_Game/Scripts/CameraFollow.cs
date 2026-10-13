using UnityEngine;

// Camara de persecucion suave. Si no se asigna objetivo, busca el auto del jugador.
[RequireComponent(typeof(Camera))]
public class CameraFollow : MonoBehaviour
{
    public Transform target;
    public float distance = 7.5f;
    public float height = 3f;
    public float positionSharpness = 6f;   // mas alto = la camara se pega mas rapido
    public float rotationSharpness = 5f;
    public float baseFov = 60f;
    public float maxFov = 78f;
    public float maxFovSpeed = 45f;        // m/s a la que se alcanza maxFov

    Camera cam;
    Rigidbody targetBody;

    void Awake() { cam = GetComponent<Camera>(); }

    void LateUpdate()
    {
        if (target == null)
        {
            RaceProgress[] all = FindObjectsOfType<RaceProgress>();
            foreach (RaceProgress r in all) if (r.isPlayer) { target = r.transform; break; }
            if (target == null) return;
        }
        if (targetBody == null) targetBody = target.GetComponent<Rigidbody>();

        Vector3 fwd = target.forward;
        fwd.y = 0f;
        if (fwd.sqrMagnitude < 0.01f) fwd = Vector3.forward;
        fwd.Normalize();

        Vector3 wanted = target.position - fwd * distance + Vector3.up * height;
        float kPos = 1f - Mathf.Exp(-positionSharpness * Time.deltaTime);
        float kRot = 1f - Mathf.Exp(-rotationSharpness * Time.deltaTime);
        transform.position = Vector3.Lerp(transform.position, wanted, kPos);

        Quaternion look = Quaternion.LookRotation(target.position + Vector3.up - transform.position);
        transform.rotation = Quaternion.Slerp(transform.rotation, look, kRot);

        float speed = targetBody != null ? targetBody.velocity.magnitude : 0f;
        float fov = Mathf.Lerp(baseFov, maxFov, Mathf.Clamp01(speed / maxFovSpeed));
        cam.fieldOfView = Mathf.Lerp(cam.fieldOfView, fov, 1f - Mathf.Exp(-3f * Time.deltaTime));
    }
}
