package Modelo;

public class Atraccion {
    private int id;
    private String nombre;
    private Zona zona;
    private boolean operativa;

    private static int contadorIds = 0;

    public Atraccion(String nombre, Zona zona) {
        this.id = ++contadorIds;
        this.nombre = nombre;
        this.zona = zona;
        this.operativa = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public boolean isOperativa() {
        return operativa;
    }

    public void setOperativa(boolean operativa) {
        this.operativa = operativa;
    }

    @Override
    public String toString() {
        String estado = operativa ? "✅" : "❌";
        return estado + " " + nombre + " [" + zona.name() + "]";
    }
}