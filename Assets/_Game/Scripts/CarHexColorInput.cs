using UnityEngine;
using TMPro; // Necesario para TMP_InputField

public class CarHexColorInput : MonoBehaviour
{
    [Header("Referencias del Auto")]
    [SerializeField] private MeshRenderer carMeshRenderer;
    [SerializeField] private int bodyMaterialIndex = 0;

    [Header("Referencias de UI")]
    [Tooltip("El Panel UI que contiene el campo de texto e instrucciones.")]
    [SerializeField] private GameObject hexInputPanel;
    
    [Tooltip("El campo de texto (TMP_InputField) donde el usuario escribe el Hex.")]
    [SerializeField] private TMP_InputField hexInputField;

    private Material targetMaterial;

    private void Awake()
    {
        if (carMeshRenderer != null)
        {
            Material[] mats = carMeshRenderer.materials;
            if (bodyMaterialIndex < mats.Length)
            {
                // Instanciamos el material para no sobrescribir el Asset original
                targetMaterial = mats[bodyMaterialIndex];
            }
        }

        // Nos aseguramos de que el panel empiece oculto
        if (hexInputPanel != null)
        {
            hexInputPanel.SetActive(false);
        }
    }

    private void Update()
    {
        // Detectar si el jugador presiona la tecla P
        if (Input.GetKeyDown(KeyCode.P))
        {
            ToggleHexPanel();
        }
    }

    /// <summary>
    /// Muestra u oculta el panel al presionar P.
    /// </summary>
    public void ToggleHexPanel()
    {
        if (hexInputPanel == null) return;

        bool isActive = !hexInputPanel.activeSelf;
        hexInputPanel.SetActive(isActive);

        if (isActive && hexInputField != null)
        {
            hexInputField.Select();
            hexInputField.ActivateInputField();
        }
    }

    /// <summary>
    /// Método llamado cuando el usuario presiona Enter o confirma el código Hex en el InputField.
    /// </summary>
    public void ApplyHexColor(string hexCode)
    {
        if (string.IsNullOrEmpty(hexCode) || targetMaterial == null) return;

        // Limpiar el código por si no se incluyó el símbolo '#'
        if (!hexCode.StartsWith("#"))
        {
            hexCode = "#" + hexCode;
        }

        // Convertir la cadena Hex a UnityEngine.Color
        if (ColorUtility.TryParseHtmlString(hexCode, out Color newColor))
        {
            SetColor(newColor);
        }
        else
        {
            Debug.LogWarning("Código Hexadecimal inválido: " + hexCode);
        }
    }

    private void SetColor(Color newColor)
    {
        if (targetMaterial.HasProperty("_BaseColor"))
        {
            targetMaterial.SetColor("_BaseColor", newColor);
        }
        else
        {
            targetMaterial.color = newColor;
        }
    }
}