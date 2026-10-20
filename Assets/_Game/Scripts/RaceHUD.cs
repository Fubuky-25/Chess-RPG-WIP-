using UnityEngine;

// HUD simple con IMGUI (no necesita Canvas ni configuracion).
public class RaceHUD : MonoBehaviour
{
    GUIStyle big, medium, small, shadow;

    static string FormatTime(float t)
    {
        if (t >= float.MaxValue * 0.5f) return "--:--.---";
        int m = (int)(t / 60f);
        float s = t - m * 60f;
        return m.ToString("00") + ":" + s.ToString("00.000");
    }

    void MakeStyles()
    {
        if (big != null) return;
        big = new GUIStyle(GUI.skin.label) { fontSize = 96, fontStyle = FontStyle.Bold, alignment = TextAnchor.MiddleCenter };
        medium = new GUIStyle(GUI.skin.label) { fontSize = 34, fontStyle = FontStyle.Bold };
        small = new GUIStyle(GUI.skin.label) { fontSize = 20 };
        shadow = new GUIStyle(big);
    }

    void Label(Rect r, string text, GUIStyle style, Color c)
    {
        Color old = GUI.color;
        GUI.color = new Color(0f, 0f, 0f, 0.7f);
        GUI.Label(new Rect(r.x + 2, r.y + 2, r.width, r.height), text, style);
        GUI.color = c;
        GUI.Label(r, text, style);
        GUI.color = old;
    }

    void OnGUI()
    {
        RaceManager rm = RaceManager.Instance;
        if (rm == null || rm.Player == null) return;
        if (rm.State == RaceManager.RaceState.ColorSelect) return; // el selector dibuja su propio panel
        MakeStyles();

        float w = Screen.width, h = Screen.height;
        RaceProgress p = rm.Player;

        // Posicion y vuelta (arriba izquierda)
        Label(new Rect(20, 10, 400, 50), "POS " + p.Position + "/" + rm.Racers.Count, medium, Color.white);
        Label(new Rect(20, 55, 400, 50), "VUELTA " + p.CurrentLap + "/" + rm.TotalLaps, medium, Color.white);

        // Tiempos (arriba derecha)
        GUIStyle right = new GUIStyle(small) { alignment = TextAnchor.UpperRight };
        Label(new Rect(w - 320, 14, 300, 30), "Tiempo  " + FormatTime(rm.RaceTime), right, Color.white);
        Label(new Rect(w - 320, 40, 300, 30), "Ultima  " + FormatTime(p.LastLap > 0 ? p.LastLap : float.MaxValue), right, Color.white);
        Label(new Rect(w - 320, 66, 300, 30), "Mejor   " + FormatTime(p.BestLap), right, Color.yellow);

        // Velocidad (abajo derecha), en km/h
        float kmh = p.body != null ? p.body.velocity.magnitude * 3.6f : 0f;
        GUIStyle speedStyle = new GUIStyle(medium) { alignment = TextAnchor.LowerRight };
        Label(new Rect(w - 320, h - 70, 300, 55), Mathf.RoundToInt(kmh) + " km/h", speedStyle, Color.white);

        // Clasificacion en vivo (izquierda)
        float y = 110;
        foreach (RaceProgress r in rm.Standings)
        {
            Color c = r.isPlayer ? Color.yellow : Color.white;
            string tag = r.Finished ? "  " + FormatTime(r.FinishTime) : "";
            Label(new Rect(20, y, 400, 28), r.Position + ". " + r.racerName + tag, small, c);
            y += 26;
        }

        // Cuenta regresiva
        if (rm.State == RaceManager.RaceState.Countdown || (rm.State == RaceManager.RaceState.Racing && rm.RaceTime < 1f))
        {
            string txt = rm.State == RaceManager.RaceState.Racing ? "YA!" : Mathf.Ceil(rm.CountdownLeft - 1f).ToString("0");
            Label(new Rect(0, h * 0.25f, w, 140), txt, big, rm.State == RaceManager.RaceState.Racing ? Color.green : Color.white);
        }

        // Resultados
        if (rm.State == RaceManager.RaceState.Finished)
        {
            Label(new Rect(0, h * 0.2f, w, 140), "META - " + p.Position + "° LUGAR", big, Color.yellow);
            Label(new Rect(0, h * 0.2f + 140, w, 40), "Enter para reiniciar", new GUIStyle(small) { alignment = TextAnchor.MiddleCenter }, Color.white);
        }
        else
        {
            Label(new Rect(20, h - 40, 600, 30), "R: volver a la pista", small, new Color(1f, 1f, 1f, 0.6f));
        }
    }
}
