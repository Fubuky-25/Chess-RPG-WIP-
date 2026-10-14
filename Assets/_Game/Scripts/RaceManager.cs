using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityStandardAssets.Vehicles.Car;

// Controla la carrera: cuenta regresiva, vueltas, posiciones y resultados.
public class RaceManager : MonoBehaviour
{
    public enum RaceState { Countdown, Racing, Finished }

    public static RaceManager Instance { get; private set; }

    public TrackPath path;
    public int laps = 3;
    public float countdownSeconds = 3f;
    [Tooltip("Distancia sobre la pista (m) donde esta la linea de salida/meta.")]
    public float startDistance = 0f;

    public List<RaceProgress> Racers { get; private set; }
    public RaceProgress Player { get; private set; }
    public RaceState State { get; private set; }
    public float CountdownLeft { get; private set; }
    public float RaceTime { get; private set; }
    public List<RaceProgress> Standings { get; private set; }
    public int TotalLaps { get { return laps; } }

    float raceStart;

    void Awake()
    {
        Instance = this;
        Racers = new List<RaceProgress>();
        Standings = new List<RaceProgress>();
    }

    void Start()
    {
        if (path == null) path = FindObjectOfType<TrackPath>();
        Racers.AddRange(FindObjectsOfType<RaceProgress>());
        foreach (RaceProgress r in Racers)
        {
            r.Init(path, startDistance, laps);
            if (r.isPlayer) Player = r;
            SetDriving(r, false);
        }
        Standings.AddRange(Racers);
        State = RaceState.Countdown;
        CountdownLeft = countdownSeconds + 1f; // +1: se muestra "GO" un segundo
    }

    void SetDriving(RaceProgress r, bool on)
    {
        AIDriver ai = r.GetComponent<AIDriver>();
        if (ai != null) ai.canDrive = on;
        CarUserControl user = r.GetComponent<CarUserControl>();
        if (user != null) user.enabled = on;
    }

    void ReleaseBrakes(RaceProgress r)
    {
        WheelCollider[] wheels = r.GetComponentsInChildren<WheelCollider>();

        foreach (WheelCollider wheel in wheels)
        {
            wheel.brakeTorque = 0f;
        }
    }
    
    void Update()
    {
        if (State == RaceState.Countdown)
        {
            CountdownLeft -= Time.deltaTime;
            if (CountdownLeft <= 1f)
            {
                State = RaceState.Racing;
                raceStart = Time.time;
                foreach (RaceProgress r in Racers)
                {
                    ReleaseBrakes(r);
                    SetDriving(r, true);
                    r.BeginRace(raceStart);
                }
            }
        }
        else
        {
            RaceTime = Time.time - raceStart;
        }

        UpdateStandings();

        if (State == RaceState.Racing && Player != null && Player.Finished)
        {
            State = RaceState.Finished;
            // El jugador deja de conducir; los rivales terminan su carrera.
            CarUserControl user = Player.GetComponent<CarUserControl>();
            if (user != null) user.enabled = false;
        }

        if (State == RaceState.Finished && Input.GetKeyDown(KeyCode.Return))
            SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
    }

    void FixedUpdate()
    {
        // En la parrilla los autos se quedan frenados; el jugador termina frenando.
        foreach (RaceProgress r in Racers)
        {
            if (r.car == null) continue;
            bool hold = State == RaceState.Countdown;
            bool playerDone = State == RaceState.Finished && r == Player;
            if (hold) r.car.Move(0f, 0f, 0f, 1f);
            else if (playerDone) r.car.Move(0f, 0f, -0.6f, 0f);
        }
    }

    void UpdateStandings()
    {
        Standings.Sort((a, b) =>
        {
            if (a.Finished != b.Finished) return a.Finished ? -1 : 1;
            if (a.Finished) return a.FinishTime.CompareTo(b.FinishTime);
            return b.TotalDistance.CompareTo(a.TotalDistance);
        });
        for (int i = 0; i < Standings.Count; i++) Standings[i].Position = i + 1;
    }
}
