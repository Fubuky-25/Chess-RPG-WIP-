using UnityEngine;
using UnityStandardAssets.Vehicles.Car;

// Lleva la cuenta de vueltas y distancia recorrida de un auto (jugador o rival).
public class RaceProgress : MonoBehaviour
{
    public string racerName = "Piloto";
    public bool isPlayer;

    [HideInInspector] public TrackPath path;
    [HideInInspector] public CarController car;
    [HideInInspector] public Rigidbody body;

    // Distancia total desde la linea de salida (0 = en la linea). Negativa en la parrilla.
    public float TotalDistance { get; private set; }
    public float PathDistance { get; private set; }  // distancia sobre la pista, 0..Length
    public float Lateral { get; private set; }       // desvio del centro de la pista (+ derecha)
    public int LapsCompleted { get; private set; }
    public bool Finished { get; private set; }
    public float FinishTime { get; private set; }
    public float BestLap { get; private set; } = float.MaxValue;
    public float LastLap { get; private set; }
    public int Position { get; set; }

    int totalLaps;
    float raceStartTime;
    float lapStartTime;
    bool running;

    public void Init(TrackPath p, float startDistance, int laps)
    {
        path = p;
        car = GetComponent<CarController>();
        body = GetComponent<Rigidbody>();
        totalLaps = laps;
        float lat;
        PathDistance = path.ProjectGlobal(transform.position, out lat);
        Lateral = lat;
        TotalDistance = path.Delta(startDistance, PathDistance);
        LapsCompleted = 0;
        Finished = false;
        running = false;
    }

    public void BeginRace(float time)
    {
        raceStartTime = time;
        lapStartTime = time;
        running = true;
    }

    void FixedUpdate()
    {
        if (path == null) return;
        float lat;
        float d = path.Project(transform.position, PathDistance, 25f, out lat);
        TotalDistance += path.Delta(PathDistance, d);
        PathDistance = d;
        Lateral = lat;

        if (!running || Finished) return;

        int laps = Mathf.Max(0, Mathf.FloorToInt(TotalDistance / path.Length));
        if (laps > LapsCompleted)
        {
            LapsCompleted = laps;
            float now = Time.time;
            LastLap = now - lapStartTime;
            if (LastLap < BestLap) BestLap = LastLap;
            lapStartTime = now;
            if (LapsCompleted >= totalLaps)
            {
                Finished = true;
                FinishTime = now - raceStartTime;
            }
        }
        else if (laps < LapsCompleted)
        {
            // Retrocedio detras de la linea: no regalar la vuelta.
            LapsCompleted = laps;
        }
    }

    // Vuelta que se esta corriendo (1..totalLaps).
    public int CurrentLap { get { return Mathf.Clamp(LapsCompleted + 1, 1, totalLaps); } }

    public float CurrentLapTime(float now) { return running ? now - lapStartTime : 0f; }
}
