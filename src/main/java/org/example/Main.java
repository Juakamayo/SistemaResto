package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    // Movemos el panel aquí para poder acceder a él desde el método refrescar
    private TilePane panelMesas;

    @Override
    public void start(Stage stage) {
        // 1. Inicializar
        DatabaseHelper.inicializarBaseDeDatos();
        DatabaseHelper.crearMesasPorDefecto();
        DatabaseHelper.crearProductosPorDefecto();

        // 2. Configurar el panel de mesas
        panelMesas = new TilePane();
        panelMesas.setPadding(new Insets(20));
        panelMesas.setHgap(20);
        panelMesas.setVgap(20);
        panelMesas.setPrefColumns(4);

        // 3. LLAMADA INICIAL PARA DIBUJAR LAS MESAS
        refrescarMesas();

        // 4. Configurar Scroll y Barra Superior
        ScrollPane scrollPane = new ScrollPane(panelMesas);
        scrollPane.setFitToWidth(true);

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #343a40;");
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button btnInventario = new Button("GESTIONAR INVENTARIO");
        btnInventario.setStyle("-fx-background-color: #fab005; -fx-text-fill: black; -fx-font-weight: bold;");
        // Nota: VentanaInventario no necesita showAndWait porque no afecta visualmente a las mesas inmediatamente
        btnInventario.setOnAction(e -> new VentanaInventario().mostrar());

        topBar.getChildren().add(btnInventario);

        // 5. Layout General
        BorderPane root = new BorderPane();
        root.setTop(topBar); // Barra arriba
        root.setCenter(scrollPane); // Mesas al centro
        root.setStyle("-fx-background-color: #f1f3f5;");

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Sistema Restaurante - Salón");
        stage.setScene(scene);
        stage.show();
    }

    // --- MÉTODO MÁGICO PARA ACTUALIZAR ---
    private void refrescarMesas() {
        // 1. Limpiamos lo que había antes para no duplicar botones
        panelMesas.getChildren().clear();

        // 2. Obtenemos datos frescos
        List<Mesa> mesas = DatabaseHelper.obtenerMesas();

        for (Mesa mesa : mesas) {
            // Verificar si hay pedidos en memoria para esta mesa
            boolean tienePedidosActivos = !GestorPedidos.getProductos(mesa.getId()).isEmpty();

            // Decidimos el estado visual: Si la DB dice ocupada O si tiene cosas en memoria
            boolean estaOcupada = mesa.getEstado().equals("OCUPADA") || tienePedidosActivos;

            String textoEstado = estaOcupada ? "OCUPADA" : "LIBRE";
            Button btnMesa = new Button(mesa.getNombre() + "\n" + textoEstado);
            btnMesa.setPrefSize(150, 100);
            btnMesa.setFont(new Font("Arial", 16));

            // Colores
            if (estaOcupada) {
                // Rojo
                btnMesa.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            } else {
                // Verde
                btnMesa.setStyle("-fx-background-color: #51cf66; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
            }

            // Acción al hacer clic
            btnMesa.setOnAction(e -> {
                // 1. Abrimos la mesa y ESPERAMOS a que se cierre (gracias a showAndWait)
                new VentanaPedido(mesa).mostrar();

                // 2. Cuando la ventana se cierra, esta línea se ejecuta inmediatamente:
                refrescarMesas();
            });

            panelMesas.getChildren().add(btnMesa);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}