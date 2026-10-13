using UnityEngine;

// Pinta la carroceria del auto para distinguir a los rivales.
public class CarPaint : MonoBehaviour
{
    public Color color = Color.red;

    void Awake()
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
