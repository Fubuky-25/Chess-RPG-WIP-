using UnityEngine;

// Pinta la carroceria del auto para distinguir a los rivales.
// SetColor permite cambiar el color en cualquier momento (por ejemplo desde el selector previo a la carrera).
public class CarPaint : MonoBehaviour
{
    public Color color = Color.red;

    void Awake()
    {
        Apply();
    }

    public void SetColor(Color newColor)
    {
        color = newColor;
        Apply();
    }

    void Apply()
    {
        var block = new MaterialPropertyBlock();
        foreach (Renderer r in GetComponentsInChildren<Renderer>())
        {
            if (!r.name.Contains("Body") || r.name.Contains("Collider")) continue;
            r.GetPropertyBlock(block);
            block.SetColor("_Color", color);
            r.SetPropertyBlock(block);
        }
    }
}
