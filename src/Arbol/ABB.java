package Arbol;

import Modelo.Atraccion;
import Modelo.Zona;
import java.util.ArrayList;
import java.util.List;

public class ABB {
    private NodoABB raiz;

    public ABB() {
        this.raiz = null;
    }

    public NodoABB getRaiz() { return raiz; }

    // ========== INSERTAR ==========
    public void insertar(Atraccion atraccion) {
        raiz = insertarRec(raiz, atraccion);
    }

    private NodoABB insertarRec(NodoABB nodo, Atraccion atraccion) {
        if (nodo == null) return new NodoABB(atraccion);

        int comp = atraccion.getNombre().compareToIgnoreCase(nodo.getAtraccion().getNombre());
        if (comp < 0) nodo.setIzquierdo(insertarRec(nodo.getIzquierdo(), atraccion));
        else if (comp > 0) nodo.setDerecho(insertarRec(nodo.getDerecho(), atraccion));
        return nodo;
    }

    // ========== BUSCAR ==========
    public Atraccion buscar(String nombre) {
        NodoABB nodo = buscarRec(raiz, nombre);
        return nodo != null ? nodo.getAtraccion() : null;
    }

    private NodoABB buscarRec(NodoABB nodo, String nombre) {
        if (nodo == null) return null;
        int comp = nombre.compareToIgnoreCase(nodo.getAtraccion().getNombre());
        if (comp == 0) return nodo;
        if (comp < 0) return buscarRec(nodo.getIzquierdo(), nombre);
        return buscarRec(nodo.getDerecho(), nombre);
    }

    // ========== RECORRIDO INORDEN ==========
    public List<Atraccion> recorridoInorden() {
        List<Atraccion> lista = new ArrayList<>();
        inordenRec(raiz, lista);
        return lista;
    }

    private void inordenRec(NodoABB nodo, List<Atraccion> lista) {
        if (nodo != null) {
            inordenRec(nodo.getIzquierdo(), lista);
            lista.add(nodo.getAtraccion());
            inordenRec(nodo.getDerecho(), lista);
        }
    }

    // ========== LISTAR POR ZONA ==========
    public List<Atraccion> listarPorZona(Zona zona) {
        List<Atraccion> lista = new ArrayList<>();
        listarPorZonaRec(raiz, zona, lista);
        return lista;
    }

    private void listarPorZonaRec(NodoABB nodo, Zona zona, List<Atraccion> lista) {
        if (nodo != null) {
            listarPorZonaRec(nodo.getIzquierdo(), zona, lista);
            if (nodo.getAtraccion().getZona() == zona) lista.add(nodo.getAtraccion());
            listarPorZonaRec(nodo.getDerecho(), zona, lista);
        }
    }

    // ========== ELIMINAR ==========
    public boolean eliminar(String nombre) {
        if (buscar(nombre) == null) return false;
        raiz = eliminarRec(raiz, nombre);
        return true;
    }

    private NodoABB eliminarRec(NodoABB nodo, String nombre) {
        if (nodo == null) return null;

        int comp = nombre.compareToIgnoreCase(nodo.getAtraccion().getNombre());
        if (comp < 0) nodo.setIzquierdo(eliminarRec(nodo.getIzquierdo(), nombre));
        else if (comp > 0) nodo.setDerecho(eliminarRec(nodo.getDerecho(), nombre));
        else {
            if (nodo.getIzquierdo() == null && nodo.getDerecho() == null) return null;
            if (nodo.getIzquierdo() == null) return nodo.getDerecho();
            if (nodo.getDerecho() == null) return nodo.getIzquierdo();

            NodoABB sucesor = encontrarMin(nodo.getDerecho());
            nodo.getAtraccion().setNombre(sucesor.getAtraccion().getNombre());
            nodo.getAtraccion().setZona(sucesor.getAtraccion().getZona());
            nodo.getAtraccion().setOperativa(sucesor.getAtraccion().isOperativa());
            nodo.setDerecho(eliminarRec(nodo.getDerecho(), sucesor.getAtraccion().getNombre()));
        }
        return nodo;
    }

    private NodoABB encontrarMin(NodoABB nodo) {
        while (nodo.getIzquierdo() != null) nodo = nodo.getIzquierdo();
        return nodo;
    }

    // ========== MODIFICAR NOMBRE (única modificación) ==========
    public boolean modificarNombre(String nombreActual, String nombreNuevo) {
        Atraccion a = buscar(nombreActual);
        if (a != null) {
            Atraccion nueva = new Atraccion(nombreNuevo, a.getZona());
            nueva.setOperativa(a.isOperativa());
            eliminar(nombreActual);
            insertar(nueva);
            return true;
        }
        return false;
    }

    // ========== CONTAR ATRACCIONES ==========
    public int contarAtracciones() {
        return contarRec(raiz);
    }

    private int contarRec(NodoABB nodo) {
        if (nodo == null) return 0;
        return 1 + contarRec(nodo.getIzquierdo()) + contarRec(nodo.getDerecho());
    }
}