package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:restaurante.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
        return conn;
    }

    // --- 1. INICIALIZACIÓN (Aquí creamos TODAS las tablas) ---
    public static void inicializarBaseDeDatos() {
        // Tablas Básicas
        String sqlCategorias = "CREATE TABLE IF NOT EXISTS categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT);";
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, precio REAL, categoria_id INTEGER);";
        String sqlMesas = "CREATE TABLE IF NOT EXISTS mesas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, estado TEXT DEFAULT 'LIBRE');";

        // Tablas de Historial (Ventas)
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas (id INTEGER PRIMARY KEY AUTOINCREMENT, mesa TEXT, fecha DATETIME DEFAULT CURRENT_TIMESTAMP, total REAL)";
        String sqlDetalle = "CREATE TABLE IF NOT EXISTS detalle_ventas (id INTEGER PRIMARY KEY AUTOINCREMENT, venta_id INTEGER, producto TEXT, precio REAL, cantidad INTEGER, FOREIGN KEY(venta_id) REFERENCES ventas(id))";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCategorias);
            stmt.execute(sqlProductos);
            stmt.execute(sqlMesas);
            stmt.execute(sqlVentas);
            stmt.execute(sqlDetalle);
        } catch (SQLException e) {
            System.out.println("Error inicializando DB: " + e.getMessage());
        }
    }

    // --- 2. DATOS POR DEFECTO ---
    public static void crearMesasPorDefecto() {
        String sqlCount = "SELECT COUNT(*) FROM mesas";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlCount)) {
            if (rs.next() && rs.getInt(1) == 0) {
                Statement s = conn.createStatement();
                // Creamos mesas iniciales LIBRES
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 1', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 2', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 3', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 4', 'LIBRE')");
            }
        } catch(Exception e) {}
    }

    public static void crearProductosPorDefecto() {
        String sqlCount = "SELECT COUNT(*) FROM productos";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlCount)) {
            if (rs.next() && rs.getInt(1) == 0) {
                Statement s = conn.createStatement();
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Coca Cola Original', 1500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Hamburguesa Clásica', 5500, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Papas Fritas', 3000, 2)");
            }
        } catch (SQLException e) {}
    }

    // --- 3. GESTIÓN DE MESAS ---
    public static void guardarMesa(String nombre) {
        String sql = "INSERT INTO mesas (nombre, estado) VALUES (?, 'LIBRE')";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void eliminarMesa(String nombre) {
        String sql = "DELETE FROM mesas WHERE nombre = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void liberarTodasLasMesas() {
        String sql = "UPDATE mesas SET estado = 'LIBRE'";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Mesa> obtenerMesas() {
        List<Mesa> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM mesas")) {
            while (rs.next()) lista.add(new Mesa(rs.getInt("id"), rs.getString("nombre"), rs.getString("estado")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // --- 4. GESTIÓN DE PRODUCTOS ---
    public static List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM productos")) {
            while (rs.next()) lista.add(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio"), "General"));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static void guardarProducto(String nombre, double precio, int categoriaId) {
        String sql = "INSERT INTO productos(nombre, precio, categoria_id) VALUES(?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre); pstmt.setDouble(2, precio); pstmt.setInt(3, categoriaId);
            pstmt.executeUpdate();
        } catch (SQLException e) {}
    }

    public static void eliminarProducto(String nombre) {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM productos WHERE nombre = ?")) {
            pstmt.setString(1, nombre); pstmt.executeUpdate();
        } catch (SQLException e) {}
    }

    // --- 5. GESTIÓN DE VENTAS E HISTORIAL ---
    public static void registrarVenta(String mesa, Map<String, Integer> conteo, double total) {
        // CORRECCIÓN: Ya no llamamos a verificarTablasVentas() porque se hace en inicializarBaseDeDatos()

        String sqlVenta = "INSERT INTO ventas(mesa, total) VALUES(?, ?)";
        String sqlDetalle = "INSERT INTO detalle_ventas(venta_id, producto, precio, cantidad) VALUES(?, ?, ?, ?)";

        try (Connection conn = connect()) {
            // Guardar Cabecera (La Venta)
            PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstmtVenta.setString(1, mesa);
            pstmtVenta.setDouble(2, total);
            pstmtVenta.executeUpdate();

            // Obtener el ID generado
            ResultSet rs = pstmtVenta.getGeneratedKeys();
            int idVenta = 0;
            if (rs.next()) idVenta = rs.getInt(1);

            // Guardar Detalles (Los Productos)
            PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle);
            for (Map.Entry<String, Integer> entry : conteo.entrySet()) {
                pstmtDetalle.setInt(1, idVenta);
                pstmtDetalle.setString(2, entry.getKey());
                pstmtDetalle.setDouble(3, 0); // Precio guardado simplificado
                pstmtDetalle.setInt(4, entry.getValue());
                pstmtDetalle.addBatch();
            }
            pstmtDetalle.executeBatch();
        } catch (SQLException e) {
            System.out.println("Error registrando venta: " + e.getMessage());
        }
    }

    public static List<VentaRow> obtenerHistorial() {
        List<VentaRow> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM ventas ORDER BY id DESC")) {
            while (rs.next()) lista.add(new VentaRow(rs.getInt("id"), rs.getString("mesa"), rs.getString("fecha"), rs.getDouble("total")));
        } catch (Exception e) {}
        return lista;
    }

    public static Map<String, Integer> obtenerDetalleVenta(int idVenta) {
        Map<String, Integer> detalle = new java.util.HashMap<>();
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("SELECT producto, cantidad FROM detalle_ventas WHERE venta_id = ?")) {
            pstmt.setInt(1, idVenta);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) detalle.put(rs.getString("producto"), rs.getInt("cantidad"));
        } catch (Exception e) {}
        return detalle;
    }

    // Clase auxiliar para la tabla visual de historial
    public static class VentaRow {
        public int id; public String mesa; public String fecha; public double total;
        public VentaRow(int id, String mesa, String fecha, double total) { this.id=id; this.mesa=mesa; this.fecha=fecha; this.total=total; }
        public int getId() { return id; } public String getMesa() { return mesa; } public String getFecha() { return fecha; } public double getTotal() { return total; }
    }

    // --- PERSISTENCIA EN TIEMPO REAL (LO QUE PIDEN AHORA) ---

    // 1. Crear tabla de pendientes (Asegúrate de que este método se llame al iniciar)
    public static void verificarTablaPendientes() {
        String sql = "CREATE TABLE IF NOT EXISTS pedidos_pendientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "mesa_id INTEGER, " +
                "producto_nombre TEXT, " +
                "precio REAL)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 2. Guardar un item individual (Se llama al dar clic al botón del producto)
    public static void guardarItemPendiente(int mesaId, Producto p) {
        String sql = "INSERT INTO pedidos_pendientes (mesa_id, producto_nombre, precio) VALUES (?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mesaId);
            pstmt.setString(2, p.getNombre());
            pstmt.setDouble(3, p.getPrecio());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 3. Borrar un item individual (Se llama al dar clic a "Quitar")
    public static void eliminarItemPendiente(int mesaId, Producto p) {
        // Borramos SOLO UNO (usando el ID más reciente que coincida) para no borrar todas las cocas si hay 3
        String sql = "DELETE FROM pedidos_pendientes WHERE id = (SELECT id FROM pedidos_pendientes WHERE mesa_id = ? AND producto_nombre = ? LIMIT 1)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mesaId);
            pstmt.setString(2, p.getNombre());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 4. Limpiar mesa (Se llama al Cobrar)
    public static void limpiarPendientesMesa(int mesaId) {
        String sql = "DELETE FROM pedidos_pendientes WHERE mesa_id = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mesaId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // 5. RECUPERAR TODO (Se llama al abrir el programa)
    public static java.util.Map<Integer, java.util.List<Producto>> recuperarPedidosActivos() {
        verificarTablaPendientes(); // Aseguramos que exista la tabla
        java.util.Map<Integer, java.util.List<Producto>> mapa = new java.util.HashMap<>();

        String sql = "SELECT * FROM pedidos_pendientes";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int mId = rs.getInt("mesa_id");
                String nom = rs.getString("producto_nombre");
                double pre = rs.getDouble("precio");

                // Reconstruimos el producto
                Producto p = new Producto(0, nom, pre, "Pendiente");

                mapa.putIfAbsent(mId, new java.util.ArrayList<>());
                mapa.get(mId).add(p);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapa;
    }
}