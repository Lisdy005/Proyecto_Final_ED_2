package Arbol;

import Modelo.Atraccion;

public class NodoABB {
    private Atraccion atraccion;
    private NodoABB izquierdo;
    private NodoABB derecho;

    public NodoABB(Atraccion atraccion) {
        this.atraccion = atraccion;
        this.izquierdo = null;
        this.derecho = null;
    }

    public Atraccion getAtraccion() {
        return atraccion;
    }

    public void setAtraccion(Atraccion atraccion) {
        this.atraccion = atraccion;
    }

    public NodoABB getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoABB izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoABB getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoABB derecho) {
        this.derecho = derecho;
    }
}