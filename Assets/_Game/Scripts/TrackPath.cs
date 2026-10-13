using UnityEngine;

// Linea de carrera cerrada: una lista de waypoints (hijos de este objeto) en el sentido de la carrera.
// La usan la IA (para seguir la pista) y el progreso de vueltas (distancia recorrida).
public class TrackPath : MonoBehaviour
{
    [Tooltip("Mitad del ancho util de la pista, en metros. Limita el desvio lateral de los autos.")]
    public float halfWidth = 8f;

    Vector3[] points;
    float[] cumulative; // cumulative[i] = distancia desde el punto 0 hasta el punto i
    float length;

    public float Length { get { EnsureBuilt(); return length; } }
    public int Count { get { EnsureBuilt(); return points.Length; } }

    void Awake() { Rebuild(); }
    void OnValidate() { points = null; }

    void EnsureBuilt() { if (points == null) Rebuild(); }

    public void Rebuild()
    {
        int n = transform.childCount;
        points = new Vector3[n];
        cumulative = new float[n];
        for (int i = 0; i < n; i++) points[i] = transform.GetChild(i).position;
        length = 0f;
        for (int i = 0; i < n; i++)
        {
            cumulative[i] = length;
            length += Vector3.Distance(points[i], points[(i + 1) % n]);
        }
    }

    public float Wrap(float d)
    {
        float L = Length;
        d %= L;
        if (d < 0f) d += L;
        return d;
    }

    // Diferencia con signo entre dos distancias, en el rango [-L/2, L/2].
    public float Delta(float from, float to)
    {
        float L = Length;
        float d = (to - from) % L;
        if (d > L * 0.5f) d -= L;
        else if (d < -L * 0.5f) d += L;
        return d;
    }

    int SegmentAt(float d, out float t)
    {
        EnsureBuilt();
        d = Wrap(d);
        int lo = 0, hi = points.Length - 1;
        while (lo < hi)
        {
            int mid = (lo + hi + 1) / 2;
            if (cumulative[mid] <= d) lo = mid; else hi = mid - 1;
        }
        float segLen = Vector3.Distance(points[lo], points[(lo + 1) % points.Length]);
        t = segLen > 0.0001f ? (d - cumulative[lo]) / segLen : 0f;
        return lo;
    }

    public Vector3 GetPoint(float d)
    {
        float t;
        int i = SegmentAt(d, out t);
        return Vector3.Lerp(points[i], points[(i + 1) % points.Length], t);
    }

    // Direccion de avance (plana) en la distancia d.
    public Vector3 GetTangent(float d)
    {
        float t;
        int i = SegmentAt(d, out t);
        Vector3 v = points[(i + 1) % points.Length] - points[i];
        v.y = 0f;
        return v.sqrMagnitude > 0.0001f ? v.normalized : Vector3.forward;
    }

    // Vector hacia la derecha de la pista en la distancia d.
    public Vector3 GetRight(float d)
    {
        Vector3 f = GetTangent(d);
        return new Vector3(f.z, 0f, -f.x);
    }

    // Distancia sobre la pista del punto mas cercano a pos, buscando solo cerca de "hint".
    // Buscar en una ventana evita que un atajo entre dos tramos de la pista cuente como avance.
    public float Project(Vector3 pos, float hint, float window, out float lateral)
    {
        EnsureBuilt();
        float best = float.MaxValue, bestD = hint, bestLat = 0f;
        for (float off = -window; off <= window; off += 2f)
        {
            float d = hint + off;
            Vector3 p = GetPoint(d);
            float dx = pos.x - p.x, dz = pos.z - p.z;
            float sq = dx * dx + dz * dz;
            if (sq < best)
            {
                best = sq;
                bestD = d;
                Vector3 r = GetRight(d);
                bestLat = dx * r.x + dz * r.z;
            }
        }
        lateral = bestLat;
        return Wrap(bestD);
    }

    // Busqueda global: solo para ubicar un auto la primera vez.
    public float ProjectGlobal(Vector3 pos, out float lateral)
    {
        EnsureBuilt();
        float best = float.MaxValue, bestD = 0f, bestLat = 0f;
        for (float d = 0f; d < length; d += 2f)
        {
            Vector3 p = GetPoint(d);
            float dx = pos.x - p.x, dz = pos.z - p.z;
            float sq = dx * dx + dz * dz;
            if (sq < best)
            {
                best = sq;
                bestD = d;
                Vector3 r = GetRight(d);
                bestLat = dx * r.x + dz * r.z;
            }
        }
        lateral = bestLat;
        return bestD;
    }

    void OnDrawGizmos()
    {
        int n = transform.childCount;
        if (n < 2) return;
        Gizmos.color = Color.yellow;
        for (int i = 0; i < n; i++)
        {
            Vector3 a = transform.GetChild(i).position;
            Vector3 b = transform.GetChild((i + 1) % n).position;
            Gizmos.DrawLine(a, b);
            Gizmos.DrawSphere(a, 0.6f);
        }
        Gizmos.color = Color.green;
        Gizmos.DrawSphere(transform.GetChild(0).position, 1.5f);
    }
}
