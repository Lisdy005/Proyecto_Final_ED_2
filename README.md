# Proyecto_Final_ED_2
Este es mi proyecto final para Estructura de Datos II. Hice un sistema de navegación para un parque de diversiones.

¿Qué hace?
Básicamente, el programa te permite buscar atracciones y encontrar la ruta más corta para ir de una a otra. Por ejemplo, si estás en la MontañaRusa y quieres ir a la FuenteSodas, el sistema te dice por dónde caminar y cuántos minutos tardas.

¿Cómo lo construí?
- Usé un ABB (Árbol Binario de Búsqueda) para guardar las atracciones ordenadas por nombre. Así es más fácil buscarlas y listarlas en orden alfabético.
- Después hice un Grafo con una matriz de adyacencia. Cada atracción es un punto y los caminos tienen un tiempo (en minutos).
- El algoritmo de Dijkstra se encarga de calcular la ruta más corta.

Qué se puede hacer?
- Ver atracciones 
- Buscar por nombre 
- Calcular ruta 
- Ver estadísticas 
- Agregar atracción 
- Eliminar atracción 
- Modificar nombre 
