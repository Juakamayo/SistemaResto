package org.example;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class VentanaInventario {

    public void mostrar() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Gestión de Inventario");

        // --- IZQUIERDA: FORMULARIO ---
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(20));
        formPanel.setPrefWidth(350); // Un poco más ancho para pantallas grandes
        formPanel.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6;");

        Label lblTitulo = new Label("Nuevo Producto");
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del producto");
        txtNombre.setStyle("-fx-font-size: 14px;");

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Precio (ej: 5000)");
        txtPrecio.setStyle("-fx-font-size: 14px;");

        ComboBox<String> cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("Bebida", "Plato", "Postre");
        cmbCategoria.getSelectionModel().selectFirst();
        cmbCategoria.setMaxWidth(Double.MAX_VALUE);
        cmbCategoria.setStyle("-fx-font-size: 14px;");

        Button btnGuardar = new Button("Guardar Producto");
        btnGuardar.setStyle("-fx-background-color: #228be6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setPrefHeight(40);

        Button btnEliminar = new Button("Eliminar Seleccionado");
        btnEliminar.setStyle("-fx-background-color: #fa5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnEliminar.setMaxWidth(Double.MAX_VALUE);
        btnEliminar.setPrefHeight(40);

        // --- CENTRO: TABLA ---
        TableView<Producto> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Para que las columnas ocupen todo el ancho

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tabla.getColumns().addAll(colNombre, colPrecio);
        actualizarTabla(tabla);

        // --- LÓGICA BOTONES ---
        btnGuardar.setOnAction(e -> {
            try {
                String nombre = txtNombre.getText();
                double precio = Double.parseDouble(txtPrecio.getText());
                int catId = cmbCategoria.getSelectionModel().getSelectedIndex() + 1;
                if (!nombre.isEmpty()) {
                    DatabaseHelper.guardarProducto(nombre, precio, catId);
                    actualizarTabla(tabla);
                    txtNombre.clear();
                    txtPrecio.clear();
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Precio inválido.");
                alert.show();
            }
        });

        btnEliminar.setOnAction(e -> {
            Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                DatabaseHelper.eliminarProducto(seleccionado.getNombre());
                actualizarTabla(tabla);
            }
        });

        formPanel.getChildren().addAll(lblTitulo, new Label("Nombre:"), txtNombre, new Label("Precio:"), txtPrecio, new Label("Categoría:"), cmbCategoria, new Separator(), btnGuardar, new Separator(), btnEliminar);

        BorderPane root = new BorderPane();
        root.setLeft(formPanel);
        root.setCenter(tabla);

        // --- CORRECCIÓN PANTALLA COMPLETA ---
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }

    private void actualizarTabla(TableView<Producto> tabla) {
        tabla.setItems(FXCollections.observableArrayList(DatabaseHelper.obtenerProductos()));
    }
}