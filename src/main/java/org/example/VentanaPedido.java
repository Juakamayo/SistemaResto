package org.example;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class VentanaPedido {
    private Mesa mesa;
    private ObservableList<Producto> cuentaItems;
    private Label lblTotal;
    private ListView<String> listaVisual;

    public VentanaPedido(Mesa mesa) {
        this.mesa = mesa;
        this.cuentaItems = GestorPedidos.getProductos(mesa.getId());
    }

    public void mostrar() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Pedido - " + mesa.getNombre());

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // --- IZQUIERDA ---
        TilePane menuPanel = new TilePane();
        menuPanel.setPadding(new Insets(15));
        menuPanel.setHgap(15);
        menuPanel.setVgap(15);
        menuPanel.setPrefColumns(4);

        for (Producto prod : DatabaseHelper.obtenerProductos()) {
            Button btnProd = new Button(prod.getNombre() + "\n$" + prod.getPrecio());
            btnProd.setPrefSize(140, 100);
            btnProd.setStyle("-fx-background-color: #228be6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

            // ACCIÓN: Agregar a memoria Y a base de datos
            btnProd.setOnAction(e -> {
                cuentaItems.add(prod);
                // NUEVO: Guardar persistencia
                DatabaseHelper.guardarItemPendiente(mesa.getId(), prod);
            });
            menuPanel.getChildren().add(btnProd);
        }
        ScrollPane scrollMenu = new ScrollPane(menuPanel);
        scrollMenu.setFitToWidth(true);

        // --- DERECHA ---
        VBox cuentaPanel = new VBox(10);
        cuentaPanel.setPadding(new Insets(15));
        cuentaPanel.setPrefWidth(400);
        cuentaPanel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;");

        Label tituloMesa = new Label("Mesa: " + mesa.getNombre());
        tituloMesa.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        listaVisual = new ListView<>();
        VBox.setVgrow(listaVisual, Priority.ALWAYS);

        Button btnQuitar = new Button("Quitar Seleccionado");
        btnQuitar.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        btnQuitar.setMaxWidth(Double.MAX_VALUE);
        btnQuitar.setOnAction(e -> quitarProductoSeleccionado());

        lblTotal = new Label("Total: $0");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #c92a2a;");

        Button btnVolver = new Button("<< Volver al Salón (Guardar)");
        btnVolver.setStyle("-fx-background-color: #868e96; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVolver.setMaxWidth(Double.MAX_VALUE);
        btnVolver.setPrefHeight(40);
        btnVolver.setOnAction(e -> stage.close());

        Button btnCocina = new Button("Enviar a Cocina");
        btnCocina.setStyle("-fx-background-color: #fab005; -fx-text-fill: black; -fx-font-weight: bold;");
        btnCocina.setMaxWidth(Double.MAX_VALUE);
        btnCocina.setPrefHeight(40);
        btnCocina.setOnAction(e -> enviarACocina());

        Button btnCerrarMesa = new Button("Cerrar Mesa & Cobrar");
        btnCerrarMesa.setStyle("-fx-background-color: #40c057; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCerrarMesa.setMaxWidth(Double.MAX_VALUE);
        btnCerrarMesa.setPrefHeight(50);
        btnCerrarMesa.setOnAction(e -> cerrarMesa(stage));

        cuentaPanel.getChildren().addAll(btnVolver, new Separator(), tituloMesa, listaVisual, btnQuitar, lblTotal, new Separator(), btnCocina, btnCerrarMesa);

        actualizarVista();
        cuentaItems.addListener((ListChangeListener<Producto>) c -> actualizarVista());

        SplitPane split = new SplitPane();
        split.getItems().addAll(scrollMenu, cuentaPanel);
        split.setDividerPositions(0.70);

        Scene scene = new Scene(split, bounds.getWidth(), bounds.getHeight());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.showAndWait();
    }

    private void actualizarVista() {
        listaVisual.getItems().clear();
        double total = 0;
        for (Producto p : cuentaItems) {
            listaVisual.getItems().add(p.getNombre() + "   ...   $" + p.getPrecio());
            total += p.getPrecio();
        }
        lblTotal.setText("Total: $" + total);
    }

    private void quitarProductoSeleccionado() {
        int index = listaVisual.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            Producto p = cuentaItems.get(index);
            // NUEVO: Eliminar de DB primero
            DatabaseHelper.eliminarItemPendiente(mesa.getId(), p);
            // Luego de memoria
            cuentaItems.remove(index);
        }
    }

    private void enviarACocina() {
        if (cuentaItems.isEmpty()) return;
        Map<String, Integer> conteo = new HashMap<>();
        for (Producto p : cuentaItems) conteo.put(p.getNombre(), conteo.getOrDefault(p.getNombre(), 0) + 1);
        Impresora.imprimirComandaCocina(mesa.getNombre(), conteo);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Enviado a impresora de cocina.");
        alert.show();
    }

    private void cerrarMesa(Stage stage) {
        if (cuentaItems.isEmpty()) { stage.close(); return; }

        double total = 0;
        Map<String, Integer> conteo = new HashMap<>();
        for (Producto p : cuentaItems) {
            total += p.getPrecio();
            conteo.put(p.getNombre(), conteo.getOrDefault(p.getNombre(), 0) + 1);
        }
        Impresora.imprimirBoleta(mesa.getNombre(), cuentaItems, total);
        DatabaseHelper.registrarVenta(mesa.getNombre(), conteo, total);

        // NUEVO: Limpiar la tabla persistente también
        DatabaseHelper.limpiarPendientesMesa(mesa.getId());

        GestorPedidos.limpiarMesa(mesa.getId());
        stage.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Mesa cobrada y registrada.");
        alert.showAndWait();
    }
}