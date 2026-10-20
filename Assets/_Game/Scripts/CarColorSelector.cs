using System.Collections.Generic;
using UnityEngine;

// Selector de color del auto del jugador, previo a la cuenta regresiva.
// RaceManager lo activa en el estado ColorSelect; al confirmar arranca la cuenta regresiva.
// El panel se dibuja con IMGUI (igual que RaceHUD): no necesita Canvas ni configuracion.
//
//  - Clic en un color, o flechas izquierda/derecha (A/D), para elegirlo. El auto se pinta en vivo.
//  - Tambien se puede escribir un codigo hex (ej. FF8800).
//  - Enter / Espacio / boton "A CORRER" para empezar. El color elegido se recuerda entre partidas.
public class CarColorSelector : MonoBehaviour
{
    const string PrefsKey = "Proto3D.CarColor";
    const int Columns = 5;

    [Tooltip("Colores disponibles en el selector.")]
    public Color[] palette =
    {
        new Color(1f, 0.85f, 0.1f),    // amarillo
        new Color(0.9f, 0.15f, 0.15f), // rojo
        new Color(0.15f, 0.4f, 0.95f), // azul
        new Color(0.1f, 0.75f, 0.3f),  // verde
        new Color(0.95f, 0.55f, 0.1f), // naranja
        new Color(0.6f, 0.2f, 0.85f),  // morado
        new Color(0.1f, 0.85f, 0.85f), // celeste
        new Color(1f, 0.45f, 0.75f),   // rosado
        new Color(0.95f, 0.95f, 0.95f),// blanco
        new Color(0.12f, 0.12f, 0.12f) // negro
    };

    CarPaint playerPaint;
    readonly List<CarPaint> rivals = new List<CarPaint>();
    readonly List<Color> rivalOriginal = new List<Color>();

    bool active;
    int selected = -1;
    Color current;
    string hex = "";
    bool typingHex;

    GUIStyle title, hint, button, field, label;

    public void Begin(RaceProgress player, List<RaceProgress> racers)
    {
        playerPaint = player.GetComponent<CarPaint>();
        if (playerPaint == null) playerPaint = player.gameObject.AddComponent<CarPaint>();

        rivals.Clear();
        rivalOriginal.Clear();
        foreach (RaceProgress r in racers)
        {
            if (r == player) continue;
            CarPaint p = r.GetComponent<CarPaint>();
            if (p == null) continue;
            rivals.Add(p);
            rivalOriginal.Add(p.color);
        }

        // Ultimo color usado; si no hay, el que ya traia el auto.
        Color start = playerPaint.color;
        string saved = PlayerPrefs.GetString(PrefsKey, "");
        Color parsed;
        if (saved.Length == 6 && ColorUtility.TryParseHtmlString("#" + saved, out parsed)) start = parsed;

        ApplyColor(start, true);
        active = true;
    }

    void Update()
    {
        if (!active || typingHex) return;

        if (Input.GetKeyDown(KeyCode.RightArrow) || Input.GetKeyDown(KeyCode.D)) Step(1);
        if (Input.GetKeyDown(KeyCode.LeftArrow) || Input.GetKeyDown(KeyCode.A)) Step(-1);
        if (Input.GetKeyDown(KeyCode.Return) || Input.GetKeyDown(KeyCode.KeypadEnter) || Input.GetKeyDown(KeyCode.Space))
            Confirm();
    }

    void Step(int dir)
    {
        int n = palette.Length;
        if (n == 0) return;
        int next = selected < 0 ? 0 : (selected + dir + n) % n;
        ApplyColor(palette[next], true);
    }

    void Confirm()
    {
        if (!active) return;
        active = false;
        PlayerPrefs.SetString(PrefsKey, ColorUtility.ToHtmlStringRGB(current));
        PlayerPrefs.Save();
        if (RaceManager.Instance != null) RaceManager.Instance.StartCountdown();
    }

    // Pinta el auto del jugador y evita que un rival quede con el mismo color.
    void ApplyColor(Color c, bool updateHexText)
    {
        current = c;
        if (playerPaint != null) playerPaint.SetColor(c);
        if (updateHexText) hex = ColorUtility.ToHtmlStringRGB(c);

        selected = -1;
        for (int i = 0; i < palette.Length; i++)
            if (Near(palette[i], c, 0.02f)) { selected = i; break; }

        ResolveRivalColors();
    }

    // Cada rival conserva su color original, salvo que se parezca al del jugador:
    // en ese caso toma un color libre de la paleta.
    void ResolveRivalColors()
    {
        var used = new List<Color> { current };
        for (int i = 0; i < rivals.Count; i++)
        {
            Color want = rivalOriginal[i];
            if (Near(want, current, 0.25f))
            {
                foreach (Color candidate in palette)
                {
                    if (Near(candidate, current, 0.25f) || IsTaken(candidate, used) || IsTaken(candidate, rivalOriginal)) continue;
                    want = candidate;
                    break;
                }
            }
            rivals[i].SetColor(want);
            used.Add(want);
        }
    }

    static bool IsTaken(Color c, List<Color> list)
    {
        foreach (Color o in list) if (Near(o, c, 0.25f)) return true;
        return false;
    }

    static bool Near(Color a, Color b, float tolerance)
    {
        return Mathf.Abs(a.r - b.r) + Mathf.Abs(a.g - b.g) + Mathf.Abs(a.b - b.b) < tolerance;
    }

    void MakeStyles()
    {
        if (title != null) return;
        title = new GUIStyle(GUI.skin.label) { fontSize = 26, fontStyle = FontStyle.Bold, alignment = TextAnchor.MiddleCenter };
        title.normal.textColor = Color.white;
        label = new GUIStyle(GUI.skin.label) { fontSize = 20, alignment = TextAnchor.MiddleRight };
        label.normal.textColor = Color.white;
        hint = new GUIStyle(GUI.skin.label) { fontSize = 16, alignment = TextAnchor.MiddleCenter };
        hint.normal.textColor = new Color(1f, 1f, 1f, 0.7f);
        button = new GUIStyle(GUI.skin.button) { fontSize = 26, fontStyle = FontStyle.Bold };
        field = new GUIStyle(GUI.skin.textField) { fontSize = 20, alignment = TextAnchor.MiddleLeft };
    }

    static void Fill(Rect r, Color c)
    {
        Color old = GUI.color;
        GUI.color = c;
        GUI.DrawTexture(r, Texture2D.whiteTexture);
        GUI.color = old;
    }

    void OnGUI()
    {
        if (!active) return;
        MakeStyles();

        // Enter dentro del campo hex solo cierra la edicion (no inicia la carrera).
        Event e = Event.current;
        if (e.type == EventType.KeyDown && (e.keyCode == KeyCode.Return || e.keyCode == KeyCode.KeypadEnter)
            && GUI.GetNameOfFocusedControl() == "hex")
        {
            GUI.FocusControl("");
            e.Use();
        }

        // Se dibuja en un espacio virtual que escala con la altura de la pantalla.
        float s = Mathf.Clamp(Screen.height / 720f, 0.55f, 1.6f);
        GUI.matrix = Matrix4x4.Scale(new Vector3(s, s, 1f));
        float vh = Screen.height / s;

        const float pw = 420f, ph = 400f;
        Rect panel = new Rect(30f, (vh - ph) * 0.5f, pw, ph);
        Fill(panel, new Color(0f, 0f, 0f, 0.7f));

        GUI.Label(new Rect(panel.x, panel.y + 14f, pw, 36f), "ELIGE EL COLOR DE TU AUTO", title);

        // Cuadricula de colores
        const float sw = 64f, gap = 12f;
        float gridW = Columns * sw + (Columns - 1) * gap;
        float x0 = panel.x + (pw - gridW) * 0.5f;
        float y0 = panel.y + 66f;
        for (int i = 0; i < palette.Length; i++)
        {
            Rect r = new Rect(x0 + (i % Columns) * (sw + gap), y0 + (i / Columns) * (sw + gap), sw, sw);
            if (i == selected) Fill(new Rect(r.x - 4f, r.y - 4f, r.width + 8f, r.height + 8f), Color.white);
            Fill(r, palette[i]);
            if (GUI.Button(r, GUIContent.none, GUIStyle.none))
            {
                GUI.FocusControl("");
                ApplyColor(palette[i], true);
            }
        }

        // Codigo hex
        float yHex = y0 + 2f * (sw + gap) + 8f;
        GUI.Label(new Rect(x0, yHex, 90f, 34f), "Hex  #", label);
        GUI.SetNextControlName("hex");
        string edited = GUI.TextField(new Rect(x0 + 96f, yHex, 130f, 34f), hex, 8, field).Replace("#", "");
        typingHex = GUI.GetNameOfFocusedControl() == "hex";
        if (edited != hex)
        {
            hex = edited;
            Color parsed;
            if (hex.Length == 6 && ColorUtility.TryParseHtmlString("#" + hex, out parsed)) ApplyColor(parsed, false);
        }
        Fill(new Rect(x0 + 240f, yHex, 34f, 34f), current);

        GUI.Label(new Rect(panel.x, yHex + 44f, pw, 24f), "Flechas o A/D: cambiar color   ·   Enter: empezar", hint);

        if (GUI.Button(new Rect(x0, panel.yMax - 74f, gridW, 56f), "¡A CORRER!", button)) Confirm();

        GUI.matrix = Matrix4x4.identity;
    }
}
