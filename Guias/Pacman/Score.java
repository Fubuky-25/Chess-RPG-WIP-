import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Score extends Actor
{
    private String texto;
    private int puntos;
    // Constructor sin parametros
    public Score(){
        this("");
    }
    // Constructor con parametros
    public Score(String titulo){
        setTexto(titulo + getPuntos());
        GreenfootImage img = new GreenfootImage(getTexto(), 20, greenfoot.Color.GREEN, greenfoot.Color.BLACK);
        setImage(img);
    }
    // Metodo heredado del juego
    public void act()
    {
        // Add your action code here.
    }
    // Mutador
    public void setTexto(String texto){
        this.texto = texto;
    }
    // Accesador
    public String getTexto(){
        return this.texto;
    }
    // Mutador
    public void setPuntos(int puntos){
        this.puntos = puntos;
    }
    // Accesador
    public int getPuntos(){
        return puntos;
    }
    public void updateImagen(){
        StringBuilder sB = new StringBuilder();
        sB.append("Score :");
        sB.append(getPuntos());
        setTexto(sB.toString());
        GreenfootImage img = new GreenfootImage(getTexto(), 20, greenfoot.Color.GREEN, greenfoot.Color.BLACK);
        setImage(img);
    }
    public void addPuntos(int puntos){
        setPuntos(getPuntos()+puntos);
        updateImagen();
    }
}
