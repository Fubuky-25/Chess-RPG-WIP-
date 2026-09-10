import greenfoot.*;

/**
 * Panel de informacion superior. Reemplaza los showText() sueltos y ajusta
 * automaticamente el tamano del texto para que nunca se corte.
 */
public class HudPanel extends Actor
{
    private static final int W = 560;
    private static final int H = 46;
    private static final int PAD = 10;

    private String info = "";
    private String message = "";

    public HudPanel() { redraw(); }

    public void setInfo(String info)
    {
        if (info != null && !info.equals(this.info)) { this.info = info; redraw(); }
    }

    public void setMessage(String message)
    {
        if (message != null && !message.equals(this.message)) { this.message = message; redraw(); }
    }

    private void redraw()
    {
        GreenfootImage img = new GreenfootImage(W, H);
        img.setColor(new Color(18, 18, 26, 235));
        img.fillRect(0, 0, W, H);
        img.setColor(new Color(215, 180, 90));
        img.drawRect(0, 0, W - 1, H - 1);

        int maxW = W - PAD * 2;
        TextUtil.drawFitted(img, info,    PAD, 4,  maxW, 14, 8, Color.WHITE);
        TextUtil.drawFitted(img, message, PAD, 25, maxW, 13, 8, new Color(240, 225, 150));

        setImage(img);
    }
}
