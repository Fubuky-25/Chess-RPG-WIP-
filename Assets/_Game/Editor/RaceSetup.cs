using System.Collections.Generic;
using System.IO;
using System.Text;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityStandardAssets.Vehicles.Car;

// Menu "Carrera": construye la escena de carrera completa con un clic.
//  - Crea la escena Assets/_Game/Scenes/Race.unity y le agrega la pista (prefabs del pack)
//  - Calcula la linea de carrera sobre el asfalto
//  - Pone el auto del jugador y los rivales IA en la parrilla
//  - Agrega el controlador de carrera (cuenta regresiva, vueltas, HUD) y la camara
// Escribe race_report.txt y race_map.png junto a la carpeta Assets para revisar el resultado.
public static class RaceSetup
{
    const string TrackPrefabsFolder = "Assets/CartoonTracksPack1/Track1/Prefabs/Track";
    const string RaceScene = "Assets/_Game/Scenes/Race.unity";
    const string CarPrefabPath = "Assets/Standard Assets/Vehicles/Car/Prefabs/Car.prefab";

    // Colliders cuyo nombre empieza asi se consideran asfalto de la pista.
    static readonly string[] AsphaltPrefixes = { "1TARMAC_oval" };

    const float Cell = 1f;          // resolucion del mapa (m)
    const float WaypointSpacing = 6f;
    const int Rivals = 5;
    const int Laps = 3;

    [MenuItem("Carrera/Crear escena de carrera (sentido A)")]
    public static void BuildA() { Build(false); }

    [MenuItem("Carrera/Crear escena de carrera (sentido B, contrario)")]
    public static void BuildB() { Build(true); }

    static void Build(bool reverse)
    {
        if (EditorApplication.isPlaying) { Debug.LogWarning("[Carrera] Detén el Play primero."); return; }
        if (!EditorSceneManager.SaveCurrentModifiedScenesIfUserWantsTo()) return;

        GameObject carPrefab = AssetDatabase.LoadAssetAtPath<GameObject>(CarPrefabPath);
        if (carPrefab == null) { Debug.LogError("[Carrera] No encuentro " + CarPrefabPath); return; }

        if (File.Exists(Path.Combine(ProjectDir(), RaceScene)) &&
            !EditorUtility.DisplayDialog("Carrera", "Ya existe " + RaceScene + ". Se va a reemplazar (pierdes los cambios hechos a mano en esa escena). ¿Continuar?", "Reemplazar", "Cancelar"))
            return;

        var report = new StringBuilder();
        var scene = EditorSceneManager.NewScene(NewSceneSetup.DefaultGameObjects, NewSceneMode.Single);
        GameObject trackRoot = new GameObject("Pista");
        string[] guids = AssetDatabase.FindAssets("t:Prefab", new[] { TrackPrefabsFolder });
        foreach (string guid in guids)
        {
            var prefab = AssetDatabase.LoadAssetAtPath<GameObject>(AssetDatabase.GUIDToAssetPath(guid));
            if (prefab == null) continue;
            var piece = (GameObject)PrefabUtility.InstantiatePrefab(prefab);
            piece.transform.SetParent(trackRoot.transform, false);
        }
        report.AppendLine("Piezas de pista: " + guids.Length);
        Physics.SyncTransforms();

        // ---- 1) Mapa de asfalto ----
        Bounds all = new Bounds();
        bool first = true;
        foreach (Collider c in Object.FindObjectsOfType<Collider>())
        {
            if (first) { all = c.bounds; first = false; } else all.Encapsulate(c.bounds);
        }
        float x0 = all.min.x, z0 = all.min.z;
        int w = Mathf.CeilToInt(all.size.x / Cell), h = Mathf.CeilToInt(all.size.z / Cell);
        bool[,] asphalt = new bool[w, h];
        string[,] names = new string[w, h];
        float top = all.max.y + 10f;

        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
            {
                RaycastHit hit;
                if (!Physics.Raycast(new Vector3(x0 + (i + 0.5f) * Cell, top, z0 + (j + 0.5f) * Cell), Vector3.down, out hit, 1000f)) continue;
                names[i, j] = hit.collider.name;
                foreach (string p in AsphaltPrefixes)
                    if (hit.collider.name.StartsWith(p)) { asphalt[i, j] = true; break; }
            }

        // Centro del asfalto (queda dentro del ovalo).
        double sx = 0, sz = 0; int count = 0;
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++)
                if (asphalt[i, j]) { sx += x0 + (i + 0.5f) * Cell; sz += z0 + (j + 0.5f) * Cell; count++; }
        if (count < 100) { Debug.LogError("[Carrera] No encontre asfalto. Revisa AsphaltPrefixes en RaceSetup.cs"); return; }
        Vector2 center = new Vector2((float)(sx / count), (float)(sz / count));
        report.AppendLine("Asfalto: " + count + " celdas, centro " + center);

        // ---- 2) Barrido angular: el punto medio del tramo de asfalto en cada direccion ----
        const int Rays = 720;
        var raw = new List<Vector2>();
        var widths = new List<float>();
        for (int a = 0; a < Rays; a++)
        {
            float ang = a * Mathf.PI * 2f / Rays;
            Vector2 dir = new Vector2(Mathf.Cos(ang), Mathf.Sin(ang));
            float bestStart = -1, bestEnd = -1, runStart = -1, lastIn = -100;
            for (float r = 0f; r < 500f; r += Cell)
            {
                Vector2 p = center + dir * r;
                int ix = (int)((p.x - x0) / Cell), iz = (int)((p.y - z0) / Cell);
                bool inside = ix >= 0 && iz >= 0 && ix < w && iz < h && asphalt[ix, iz];
                if (inside)
                {
                    if (runStart < 0) runStart = r;
                    lastIn = r;
                }
                else if (runStart >= 0 && r - lastIn > 4f)
                {
                    if (lastIn - runStart > bestEnd - bestStart) { bestStart = runStart; bestEnd = lastIn; }
                    runStart = -1;
                }
            }
            if (runStart >= 0 && lastIn - runStart > bestEnd - bestStart) { bestStart = runStart; bestEnd = lastIn; }
            if (bestEnd - bestStart < 4f) continue;
            raw.Add(center + dir * ((bestStart + bestEnd) * 0.5f));
            widths.Add(bestEnd - bestStart);
        }
        if (raw.Count < 50) { Debug.LogError("[Carrera] No pude trazar la linea (" + raw.Count + " puntos)."); return; }

        // Descartar rayos raros (por ejemplo los que se cuelan por un desvio hacia boxes: tramo mucho mas ancho que lo normal).
        var sortedW = new List<float>(widths);
        sortedW.Sort();
        float medianW = sortedW[sortedW.Count / 2];
        var filtered = new List<Vector2>();
        float widthSum = 0f;
        for (int i = 0; i < raw.Count; i++)
        {
            if (widths[i] > medianW * 1.5f) continue;
            filtered.Add(raw[i]);
            widthSum += widths[i];
        }
        // Descartar saltos: puntos lejos del promedio de sus vecinos.
        var clean = new List<Vector2>();
        for (int i = 0; i < filtered.Count; i++)
        {
            Vector2 avg = Vector2.zero;
            for (int k = -4; k <= 4; k++) if (k != 0) avg += filtered[(i + k + filtered.Count) % filtered.Count];
            avg /= 8f;
            if (Vector2.Distance(avg, filtered[i]) < 5f) clean.Add(filtered[i]);
        }
        report.AppendLine("Rayos validos: " + raw.Count + " | tras filtros: " + clean.Count);
        if (clean.Count < 50) { Debug.LogError("[Carrera] Muy pocos puntos tras filtrar."); return; }
        raw = clean;
        float avgWidth = widthSum / Mathf.Max(1, filtered.Count);
        report.AppendLine("Ancho medio de pista: " + avgWidth.ToString("F1") + " m (mediana " + medianW.ToString("F1") + ")");

        // Suavizar (promedio circular) y remuestrear a espaciado uniforme.
        List<Vector2> line = raw;
        for (int pass = 0; pass < 3; pass++)
        {
            var sm = new List<Vector2>(line.Count);
            for (int i = 0; i < line.Count; i++)
            {
                Vector2 acc = Vector2.zero;
                for (int k = -4; k <= 4; k++) acc += line[(i + k + line.Count) % line.Count];
                sm.Add(acc / 9f);
            }
            line = sm;
        }
        if (reverse) line.Reverse();

        float total = 0f;
        for (int i = 0; i < line.Count; i++) total += Vector2.Distance(line[i], line[(i + 1) % line.Count]);
        int n = Mathf.Max(20, Mathf.RoundToInt(total / WaypointSpacing));
        var points = new List<Vector2>(n);
        float step = total / n, walked = 0f, target = 0f;
        for (int i = 0; i < line.Count && points.Count < n; i++)
        {
            Vector2 a0 = line[i], a1 = line[(i + 1) % line.Count];
            float seg = Vector2.Distance(a0, a1);
            while (points.Count < n && target <= walked + seg)
            {
                points.Add(Vector2.Lerp(a0, a1, seg > 0f ? (target - walked) / seg : 0f));
                target += step;
            }
            walked += seg;
        }
        report.AppendLine("Longitud de la linea: " + total.ToString("F0") + " m | waypoints: " + points.Count);

        // ---- 3) Crear objeto con los waypoints ----
        GameObject pathGo = new GameObject("RacingLine");
        TrackPath path = pathGo.AddComponent<TrackPath>();
        path.halfWidth = Mathf.Max(4f, avgWidth * 0.5f * 0.9f);
        for (int i = 0; i < points.Count; i++)
        {
            var wp = new GameObject("WP_" + i.ToString("000")).transform;
            wp.SetParent(pathGo.transform, false);
            wp.position = new Vector3(points[i].x, GroundY(points[i], all.max.y), points[i].y);
        }
        path.Rebuild();

        // ---- 4) Linea de salida ----
        Vector3 startRef = new Vector3(points[0].x, 0f, points[0].y);
        bool foundStart = false;
        foreach (Renderer r in Object.FindObjectsOfType<Renderer>())
        {
            if (r.name.StartsWith("startlights_body")) { startRef = r.bounds.center; foundStart = true; break; }
        }
        foreach (Transform t in Object.FindObjectsOfType<Transform>())
            if (t.name.ToLower().Contains("start")) report.AppendLine("  objeto '" + t.name + "' en " + t.position.ToString("F1"));
        float lat;
        float startDist = path.ProjectGlobal(startRef, out lat);
        report.AppendLine("Linea de salida: " + (foundStart ? "semaforo" : "primer waypoint (no hallé semaforo)") + " en " + startRef.ToString("F1")
            + " | distancia " + startDist.ToString("F0") + " | desvio del centro " + lat.ToString("F1") + " m");

        // ---- 5) Autos en la parrilla ----
        Color[] colors = { new Color(0.9f, 0.15f, 0.15f), new Color(0.15f, 0.4f, 0.95f), new Color(0.1f, 0.75f, 0.3f),
                           new Color(0.95f, 0.55f, 0.1f), new Color(0.6f, 0.2f, 0.85f), new Color(0.1f, 0.85f, 0.85f) };
        int slots = Rivals + 1;
        int playerSlot = 2;
        int rivalIndex = 0;
        Transform playerT = null;
        GameObject carsRoot = new GameObject("Autos");
        for (int s = 0; s < slots; s++)
        {
            int row = s / 2;
            float side = (s % 2 == 0) ? -1f : 1f;
            float dist = startDist - 8f - row * 10f - (side > 0 ? 4f : 0f);
            Vector3 pos = path.GetPoint(dist) + path.GetRight(dist) * (side * Mathf.Min(3.2f, path.halfWidth * 0.4f));
            pos.y = GroundY(new Vector2(pos.x, pos.z), all.max.y) + 0.9f;
            Quaternion rot = Quaternion.LookRotation(path.GetTangent(dist), Vector3.up);

            GameObject car = (GameObject)PrefabUtility.InstantiatePrefab(carPrefab);
            car.transform.SetParent(carsRoot.transform, true);
            car.transform.SetPositionAndRotation(pos, rot);

            RaceProgress prog = car.AddComponent<RaceProgress>();
            CarRecovery rec = car.AddComponent<CarRecovery>();
            CarPaint paint = car.AddComponent<CarPaint>();
            paint.color = colors[s % colors.Length];
            CarUserControl user = car.GetComponent<CarUserControl>();

            if (s == playerSlot)
            {
                car.name = "Jugador";
                prog.isPlayer = true;
                prog.racerName = "Tú";
                rec.allowManual = true;
                if (user == null) user = car.AddComponent<CarUserControl>();
                paint.color = new Color(1f, 0.85f, 0.1f);
                playerT = car.transform;
            }
            else
            {
                rivalIndex++;
                car.name = "Rival " + rivalIndex;
                prog.racerName = "Rival " + rivalIndex;
                if (user != null) Object.DestroyImmediate(user);
                AIDriver ai = car.AddComponent<AIDriver>();
                ai.maxSpeed = 34f + rivalIndex * 1.6f;          // 122..150 km/h
                ai.cornerGrip = 9f + rivalIndex * 0.6f;
                ai.preferredOffset = ((rivalIndex % 3) - 1) * 2.5f;
            }
        }

        // ---- 6) Controlador de carrera y camara ----
        GameObject race = new GameObject("Carrera");
        RaceManager rm = race.AddComponent<RaceManager>();
        rm.path = path;
        rm.laps = Laps;
        rm.startDistance = startDist;
        race.AddComponent<RaceHUD>();

        Camera cam = Camera.main != null ? Camera.main : Object.FindObjectOfType<Camera>();
        if (cam != null && playerT != null)
        {
            CameraFollow follow = cam.GetComponent<CameraFollow>();
            if (follow == null) follow = cam.gameObject.AddComponent<CameraFollow>();
            follow.target = playerT;
            cam.transform.position = playerT.position - playerT.forward * follow.distance + Vector3.up * follow.height;
            cam.transform.LookAt(playerT.position + Vector3.up);
        }

        // ---- 7) Guardar y registrar la escena ----
        EditorSceneManager.SaveScene(scene, RaceScene);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(RaceScene, true) };

        WriteMap(w, h, x0, z0, names, asphalt, points, startRef);
        File.WriteAllText(Path.Combine(ProjectDir(), "race_report.txt"), report.ToString());
        Debug.Log("[Carrera] Escena lista: " + RaceScene + "\n" + report);
    }

    static string ProjectDir() { return Path.GetDirectoryName(Application.dataPath); }

    static float GroundY(Vector2 xz, float maxY)
    {
        RaycastHit[] hits = Physics.RaycastAll(new Vector3(xz.x, maxY + 10f, xz.y), Vector3.down, 1000f);
        float best = float.NaN;
        float bestDist = float.MaxValue;
        foreach (RaycastHit hit in hits)
        {
            bool asphaltHit = false;
            foreach (string p in AsphaltPrefixes) if (hit.collider.name.StartsWith(p)) asphaltHit = true;
            if (asphaltHit && hit.distance < bestDist) { bestDist = hit.distance; best = hit.point.y; }
        }
        if (float.IsNaN(best) && hits.Length > 0)
        {
            System.Array.Sort(hits, (a, b) => a.distance.CompareTo(b.distance));
            best = hits[0].point.y;
        }
        return float.IsNaN(best) ? 0f : best;
    }

    // PNG para revisar: cada superficie de un color, asfalto claro, waypoints en rojo, salida en verde.
    static void WriteMap(int w, int h, float x0, float z0, string[,] names, bool[,] asphalt, List<Vector2> pts, Vector3 start)
    {
        var tex = new Texture2D(w, h);
        var colors = new Dictionary<string, Color>();
        var rnd = new System.Random(7);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
            {
                Color c = Color.black;
                string nm = names[i, j];
                if (nm != null && !colors.TryGetValue(nm, out c))
                {
                    c = new Color(0.2f + 0.6f * (float)rnd.NextDouble(), 0.2f + 0.6f * (float)rnd.NextDouble(), 0.2f + 0.6f * (float)rnd.NextDouble());
                    colors[nm] = c;
                }
                if (asphalt[i, j]) c = Color.white;
                tex.SetPixel(i, j, c);
            }
        foreach (Vector2 p in pts) Dot(tex, (int)((p.x - x0) / Cell), (int)((p.y - z0) / Cell), 2, Color.red);
        Dot(tex, (int)((start.x - x0) / Cell), (int)((start.z - z0) / Cell), 5, Color.green);
        tex.Apply();
        File.WriteAllBytes(Path.Combine(ProjectDir(), "race_map.png"), tex.EncodeToPNG());
        Object.DestroyImmediate(tex);

        var legend = new StringBuilder();
        foreach (var kv in colors) legend.AppendLine(kv.Key);
        File.WriteAllText(Path.Combine(ProjectDir(), "race_map_legend.txt"), legend.ToString());
    }

    static void Dot(Texture2D t, int cx, int cy, int r, Color c)
    {
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
            {
                int px = cx + x, py = cy + y;
                if (px >= 0 && py >= 0 && px < t.width && py < t.height) t.SetPixel(px, py, c);
            }
    }
}
