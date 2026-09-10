import greenfoot.*;

/**
 * Utilidad de texto. Greenfoot no permite medir cadenas con drawString, pero el
 * constructor GreenfootImage(texto, tamano, color, fondo) SI devuelve una imagen
 * del ancho real del texto. Lo usamos para que ningun texto se salga del panel.
 */
public class TextUtil
{
    /**
     * Dibuja el texto encajandolo en maxWidth: baja el tamano de fuente y, si aun
     * asi no cabe, lo recorta con puntos suspensivos.
     * (x, y) es la esquina SUPERIOR IZQUIERDA del texto.
     */
    public static void drawFitted(GreenfootImage canvas, String text, int x, int y,
                                  int maxWidth, int maxSize, int minSize, Color color)
    {
        if (text == null || text.length() == 0) return;

        for (int size = maxSize; size >= minSize; size--) {
            GreenfootImage t = new GreenfootImage(text, size, color, null);
            if (t.getWidth() <= maxWidth) {
                canvas.drawImage(t, x, y);
                return;
            }
        }

        // No cabe ni en el tamano minimo: recortar
        String s = text;
        while (s.length() > 4) {
            s = s.substring(0, s.length() - 1);
            GreenfootImage t = new GreenfootImage(s + "...", minSize, color, null);
            if (t.getWidth() <= maxWidth) { canvas.drawImage(t, x, y); return; }
        }
    }

    /** Ancho en pixeles que ocuparia el texto a ese tamano. */
    public static int widthOf(String text, int size)
    {
        if (text == null || text.length() == 0) return 0;
        return new GreenfootImage(text, size, Color.WHITE, null).getWidth();
    }
}
