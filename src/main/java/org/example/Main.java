package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Main extends Application {

    private TilePane panelMesas;

    @Override
    public void start(Stage stage) {

        DatabaseHelper.inicializarBaseDeDatos();
        DatabaseHelper.crearMesasPorDefecto();
        DatabaseHelper.crearProductosPorDefecto();

        // --- NUEVO: RECUPERAR MEMORIA ---
        // Recuperamos los pedidos pendientes de la base de datos
        java.util.Map<Integer, java.util.List<Producto>> pendientes = DatabaseHelper.recuperarPedidosActivos();

        // Llenamos el GestorPedidos con lo que encontramos
        for (Integer mesaId : pendientes.keySet()) {
            GestorPedidos.getProductos(mesaId).addAll(pendientes.get(mesaId));
        }

        // 2. Configurar el panel
        panelMesas = new TilePane();
        panelMesas.setPadding(new Insets(20));
        panelMesas.setHgap(20);
        panelMesas.setVgap(20);
        panelMesas.setPrefColumns(4);

        refrescarMesas();

        ScrollPane scrollPane = new ScrollPane(panelMesas);
        scrollPane.setFitToWidth(true);

        // --- BARRA SUPERIOR ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #343a40;");
        topBar.setAlignment(Pos.CENTER_RIGHT);

        // Botón: Nueva Mesa
        Button btnNuevaMesa = new Button("+ NUEVA MESA");
        btnNuevaMesa.setStyle("-fx-background-color: #20c997; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNuevaMesa.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("Mesa " + (DatabaseHelper.obtenerMesas().size() + 1));
            dialog.setTitle("Agregar Mesa");
            dialog.setHeaderText("Nombre de la nueva mesa:");
            dialog.showAndWait().ifPresent(nombre -> {
                DatabaseHelper.guardarMesa(nombre);
                refrescarMesas();
            });
        });

        // Botón: Eliminar Mesa (NUEVO)
        Button btnEliminarMesa = new Button("- ELIMINAR MESA");
        btnEliminarMesa.setStyle("-fx-background-color: #fa5252; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEliminarMesa.setOnAction(e -> mostrarDialogoEliminar());

        // Botón: Inventario
        Button btnInventario = new Button("INVENTARIO");
        btnInventario.setStyle("-fx-background-color: #fab005; -fx-text-fill: black; -fx-font-weight: bold;");
        btnInventario.setOnAction(e -> new VentanaInventario().mostrar());

        // Botón: Historial
        Button btnHistorial = new Button("HISTORIAL");
        btnHistorial.setStyle("-fx-background-color: #868e96; -fx-text-fill: white; -fx-font-weight: bold;");
        btnHistorial.setOnAction(e -> new VentanaHistorial().mostrar());

        topBar.getChildren().addAll(btnNuevaMesa, btnEliminarMesa, btnInventario, btnHistorial);

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(scrollPane);
        root.setStyle("-fx-background-color: #f1f3f5;");

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Sistema Restaurante - Salón");
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarDialogoEliminar() {
        // 1. Obtenemos los nombres de las mesas actuales
        List<Mesa> mesas = DatabaseHelper.obtenerMesas();
        List<String> nombres = new java.util.ArrayList<>();
        for (Mesa m : mesas) nombres.add(m.getNombre());

        if (nombres.isEmpty()) return;

        // 2. Creamos un diálogo de selección
        ChoiceDialog<String> dialog = new ChoiceDialog<>(nombres.get(0), nombres);
        dialog.setTitle("Eliminar Mesa");
        dialog.setHeaderText("Selecciona la mesa que quieres borrar permanentemente:");
        dialog.setContentText("Mesa:");

        // 3. Si el usuario elige una y da Aceptar
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombreMesa -> {
            DatabaseHelper.eliminarMesa(nombreMesa);
            refrescarMesas(); // Actualizamos la pantalla
        });
    }

    private void refrescarMesas() {
        panelMesas.getChildren().clear();
        List<Mesa> mesas = DatabaseHelper.obtenerMesas();

        for (Mesa mesa : mesas) {
            boolean tienePedidosActivos = !GestorPedidos.getProductos(mesa.getId()).isEmpty();
            boolean estaOcupada = mesa.getEstado().equals("OCUPADA") || tienePedidosActivos;

            String textoEstado = estaOcupada ? "OCUPADA" : "LIBRE";
            Button btnMesa = new Button(mesa.getNombre() + "\n" + textoEstado);
            btnMesa.setPrefSize(150, 100);
            btnMesa.setFont(new Font("Arial", 16));

            if (estaOcupada) {
                btnMesa.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            } else {
                btnMesa.setStyle("-fx-background-color: #51cf66; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            }

            btnMesa.setOnAction(e -> {
                new VentanaPedido(mesa).mostrar();
                refrescarMesas();
            });

            panelMesas.getChildren().add(btnMesa);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}