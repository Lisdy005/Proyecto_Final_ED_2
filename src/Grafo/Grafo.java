package Grafo;

import Modelo.Atraccion;
import java.util.*;

public class Grafo {
    private int capacidad;
    private int numNodos;
    private double[][] matrizAdyacencia;
    private List<Atraccion> atracciones;  // posición = id - 1

    public Grafo(int capacidadInicial) {
        this.capacidad = capacidadInicial;
        this.numNodos = 0;
        this.matrizAdyacencia = new double[capacidadInicial][capacidadInicial];
        this.atracciones = new ArrayList<>();

        // Inicializar matriz con infinito (no hay conexiones)
        for (int i = 0; i < capacidadInicial; i++) {
            for (int j = 0; j < capacidadInicial; j++) {
                matrizAdyacencia[i][j] = Double.POSITIVE_INFINITY;
            }
            matrizAdyacencia[i][i] = 0;  // Distancia de un nodo a sí mismo es 0
        }
    }

    // 1. AGREGAR ATRACCIÓN
    public void agregarAtraccion(Atraccion a) {
        int id = a.getId();

        if (id > capacidad) {
            expandirCapacidad(id);
        }

        while (atracciones.size() < id) {
            atracciones.add(null);
        }

        if (atracciones.get(id - 1) == null) {
            atracciones.set(id - 1, a);
            numNodos++;
        }
    }

    // 2. ELIMINAR ATRACCIÓN
    public void eliminarAtraccion(int id) {
        if (id <= capacidad && id - 1 < atracciones.size()) {
            atracciones.set(id - 1, null);

            for (int i = 0; i < capacidad; i++) {
                matrizAdyacencia[id - 1][i] = Double.POSITIVE_INFINITY;
                matrizAdyacencia[i][id - 1] = Double.POSITIVE_INFINITY;
            }
            matrizAdyacencia[id - 1][id - 1] = 0;
            numNodos--;
        }
    }

    // 3. AGREGAR CAMINO (ARISTA)
    public void agregarCamino(int idOrigen, int idDestino, double peso) {
        if (idOrigen <= capacidad && idDestino <= capacidad) {
            matrizAdyacencia[idOrigen - 1][idDestino - 1] = peso;
            matrizAdyacencia[idDestino - 1][idOrigen - 1] = peso;  // No dirigido
        }
    }

    public void eliminarCamino(int idOrigen, int idDestino) {
        if (idOrigen <= capacidad && idDestino <= capacidad) {
            matrizAdyacencia[idOrigen - 1][idDestino - 1] = Double.POSITIVE_INFINITY;
            matrizAdyacencia[idDestino - 1][idOrigen - 1] = Double.POSITIVE_INFINITY;
        }
    }

    // 4. OBTENER TIEMPO DE UN CAMINO
    public double obtenerTiempoCamino(int idOrigen, int idDestino) {
        if (idOrigen <= capacidad && idDestino <= capacidad) {
            return matrizAdyacencia[idOrigen - 1][idDestino - 1];
        }
        return Double.POSITIVE_INFINITY;
    }

    // 5. ALGORITMO DE DIJKSTRA
    public ResultadoDijkstra dijkstra(int idOrigen, int idDestino) {
        int origenIdx = idOrigen - 1;
        int destinoIdx = idDestino - 1;

        int n = capacidad;
        double[] distancia = new double[n];
        int[] anterior = new int[n];
        boolean[] visitado = new boolean[n];

        // Inicializar
        Arrays.fill(distancia, Double.POSITIVE_INFINITY);
        Arrays.fill(anterior, -1);
        distancia[origenIdx] = 0;

        for (int i = 0; i < n; i++) {
            // Encontrar el nodo no visitado con distancia mínima
            int u = -1;
            double minDist = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (!visitado[j] && distancia[j] < minDist) {
                    minDist = distancia[j];
                    u = j;
                }
            }

            if (u == -1) break;  // No hay más nodos alcanzables
            visitado[u] = true;

            // Actualizar distancias de los vecinos
            for (int v = 0; v < n; v++) {
                if (!visitado[v] && matrizAdyacencia[u][v] < Double.POSITIVE_INFINITY) {
                    double nuevaDist = distancia[u] + matrizAdyacencia[u][v];
                    if (nuevaDist < distancia[v]) {
                        distancia[v] = nuevaDist;
                        anterior[v] = u;
                    }
                }
            }
        }

        // Reconstruir el camino desde destino hasta origen
        List<Integer> camino = new ArrayList<>();
        double tiempoTotal = distancia[destinoIdx];

        if (tiempoTotal < Double.POSITIVE_INFINITY) {
            int actual = destinoIdx;
            while (actual != -1) {
                camino.add(0, actual + 1);  // Convertir índice a ID
                actual = anterior[actual];
            }
        }

        return new ResultadoDijkstra(camino, tiempoTotal);
    }

    // 6. LISTAR TODOS LOS CAMINOS
    public List<String> listarCaminos() {
        List<String> caminos = new ArrayList<>();
        for (int i = 0; i < capacidad; i++) {
            for (int j = i + 1; j < capacidad; j++) {
                if (matrizAdyacencia[i][j] < Double.POSITIVE_INFINITY && matrizAdyacencia[i][j] > 0) {
                    Atraccion a1 = atracciones.get(i);
                    Atraccion a2 = atracciones.get(j);
                    if (a1 != null && a2 != null) {
                        caminos.add(a1.getNombre() + " ↔ " + a2.getNombre() + " (" + (int)matrizAdyacencia[i][j] + " min)");
                    }
                }
            }
        }
        return caminos;
    }

    // 7. OBTENER ATRACCIÓN POR ID
    public Atraccion getAtraccionPorId(int id) {
        if (id <= atracciones.size() && id > 0) {
            return atracciones.get(id - 1);
        }
        return null;
    }

    // 8. MÉTODOS AUXILIARES
    private void expandirCapacidad(int nuevoId) {
        int nuevaCapacidad = Math.max(capacidad * 2, nuevoId + 5);
        double[][] nuevaMatriz = new double[nuevaCapacidad][nuevaCapacidad];

        // Inicializar nueva matriz
        for (int i = 0; i < nuevaCapacidad; i++) {
            for (int j = 0; j < nuevaCapacidad; j++) {
                nuevaMatriz[i][j] = Double.POSITIVE_INFINITY;
            }
            nuevaMatriz[i][i] = 0;
        }

        // Copiar matriz antigua
        for (int i = 0; i < capacidad; i++) {
            System.arraycopy(matrizAdyacencia[i], 0, nuevaMatriz[i], 0, capacidad);
        }

        this.capacidad = nuevaCapacidad;
        this.matrizAdyacencia = nuevaMatriz;
    }

    public int getCapacidad() { return capacidad; }
    public int getNumNodos() { return numNodos; }

    // CLASE INTERNA PARA RESULTADO DE DIJKSTRA
    public static class ResultadoDijkstra {
        private List<Integer> camino;   // Lista de IDs en orden
        private double tiempoTotal;      // Tiempo total en minutos

        public ResultadoDijkstra(List<Integer> camino, double tiempoTotal) {
            this.camino = camino;
            this.tiempoTotal = tiempoTotal;
        }

        public List<Integer> getCamino() { return camino; }
        public double getTiempoTotal() { return tiempoTotal; }

        public boolean hayCamino() {
            return !camino.isEmpty() && tiempoTotal < Double.POSITIVE_INFINITY;
        }

        public int getNumeroDeParadas() {
            return camino.size();
        }
    }
}

