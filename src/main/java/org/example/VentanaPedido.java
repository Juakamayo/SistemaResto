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

        // 1. OBTENER TAMAÑO DE PANTALLA
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        // 2. CONFIGURAR VENTANA AL TAMAÑO DE LA PANTALLA
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // --- IZQUIERDA: MENÚ ---
        TilePane menuPanel = new TilePane();
        menuPanel.setPadding(new Insets(15));
        menuPanel.setHgap(15);
        menuPanel.setVgap(15);
        menuPanel.setPrefColumns(4);

        for (Producto prod : DatabaseHelper.obtenerProductos()) {
            Button btnProd = new Button(prod.getNombre() + "\n$" + prod.getPrecio());
            btnProd.setPrefSize(140, 100);
            btnProd.setStyle("-fx-background-color: #228be6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            btnProd.setOnAction(e -> cuentaItems.add(prod));
            menuPanel.getChildren().add(btnProd);
        }
        ScrollPane scrollMenu = new ScrollPane(menuPanel);
        scrollMenu.setFitToWidth(true);

        // --- DERECHA: CUENTA ---
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

        cuentaPanel.getChildren().addAll(
                btnVolver, new Separator(), tituloMesa, listaVisual, btnQuitar, lblTotal,
                new Separator(), btnCocina, btnCerrarMesa
        );

        actualizarVista();
        cuentaItems.addListener((ListChangeListener<Producto>) c -> actualizarVista());

        SplitPane split = new SplitPane();
        split.getItems().addAll(scrollMenu, cuentaPanel);
        split.setDividerPositions(0.70);

        // 3. CREAR ESCENA
        Scene scene = new Scene(split, bounds.getWidth(), bounds.getHeight());
        stage.setScene(scene);

        // ACTIVAR PANTALLA COMPLETA/ <<---- FULLSCREEN REAL
        stage.setMaximized(true);       // <<---- OPCIONAL para asegurar

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
        if (index >= 0) cuentaItems.remove(index);
    }

    private void enviarACocina() {
        if (cuentaItems.isEmpty()) return;
        Map<String, Integer> conteo = new HashMap<>();
        for (Producto p : cuentaItems) {
            conteo.put(p.getNombre(), conteo.getOrDefault(p.getNombre(), 0) + 1);
        }
        StringBuilder ticket = new StringBuilder();
        ticket.append("--- PEDIDO COCINA ---\n Mesa: ").append(mesa.getNombre()).append("\n---------------------\n");
        for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
            ticket.append(entry.getValue()).append(" x ").append(entry.getKey()).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, ticket.toString());
        alert.showAndWait();
    }

    private void cerrarMesa(Stage stage) {
        GestorPedidos.limpiarMesa(mesa.getId());
        stage.close();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Mesa cerrada y cobrada.");
        alert.showAndWait();
    }
}