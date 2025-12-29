package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.Map;

public class GestorPedidos {
    // Un mapa que vincula: ID de Mesa -> Lista de Productos
    private static Map<Integer, ObservableList<Producto>> pedidosEnCurso = new HashMap<>();

    public static ObservableList<Producto> getProductos(int idMesa) {
        // Si no existe una lista para esta mesa, creamos una nueva vacía
        if (!pedidosEnCurso.containsKey(idMesa)) {
            pedidosEnCurso.put(idMesa, FXCollections.observableArrayList());
        }
        // Devolvemos la lista viva (si agregas algo aquí, se queda guardado en el mapa)
        return pedidosEnCurso.get(idMesa);
    }

    // Opcional: Para limpiar la mesa cuando se cobre
    public static void limpiarMesa(int idMesa) {
        if (pedidosEnCurso.containsKey(idMesa)) {
            pedidosEnCurso.get(idMesa).clear();
        }
    }
}