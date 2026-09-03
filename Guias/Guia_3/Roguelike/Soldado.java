import greenfoot.*;

public abstract class Soldado extends Actor
{
    protected int vida;
    protected int vidaMaxima;
    protected int velocidad;

    public Soldado(int vida, int velocidad)
    {
        this.vida = vida;
        this.vidaMaxima = vida;
        this.velocidad = velocidad;
    }

    public void recibirDanio(int cantidad)
    {
        vida -= cantidad;
        if (vida <= 0)
        {
            morir();
        }
    }

    public int getVida()
    {
        return vida;
    }

    public int getVidaMaxima()
    {
        return vidaMaxima;
    }

    protected void morir()
    {
        World mundo = getWorld();
        if (mundo != null)
        {
            mundo.removeObject(this);
        }
    }
}
