package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static void inicializarBaseDeDatos() {
        // Tablas base
        String sqlCategorias = "CREATE TABLE IF NOT EXISTS categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT);";
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, precio REAL, categoria_id INTEGER);";
        String sqlMesas = "CREATE TABLE IF NOT EXISTS mesas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, estado TEXT DEFAULT 'LIBRE');";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCategorias);
            stmt.execute(sqlProductos);
            stmt.execute(sqlMesas);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- CARGA DE DATOS MASIVA (Para que no te des la lata) ---
    public static void crearProductosPorDefecto() {
        String sqlCount = "SELECT COUNT(*) FROM productos";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCount)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Llenando inventario inicial...");
                Statement s = conn.createStatement();

                // Bebidas
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Coca Cola Original', 1500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Coca Cola Zero', 1500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Fanta', 1500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Sprite', 1500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Agua Mineral', 1200, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Cerveza Corona', 2500, 1)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Jugo Natural', 2000, 1)");

                // Platos de Fondo
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Hamburguesa Clásica', 5500, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Hamburguesa Doble Queso', 6500, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Papas Fritas Medianas', 3000, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Papas Fritas Familiares', 5000, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Pizza Pepperoni', 8000, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Pizza Vegetariana', 7500, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Lomo a lo Pobre', 9000, 2)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Ensalada César', 4500, 2)");

                // Postres
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Helado Vainilla', 2000, 3)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Brownie con Helado', 3500, 3)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Cheesecake', 3000, 3)");
                s.execute("INSERT INTO productos (nombre, precio, categoria_id) VALUES ('Café Espresso', 1500, 3)");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void crearMesasPorDefecto() {
        // (Tu código de mesas anterior se mantiene igual, lo simplifico aquí para ahorrar espacio)
        // Si ya tienes mesas en tu DB, este código no hará nada, así que está bien.
        String sqlCount = "SELECT COUNT(*) FROM mesas";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlCount)) {
            if (rs.next() && rs.getInt(1) == 0) {
                Statement s = conn.createStatement();
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 1', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 2', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 3', 'LIBRE')");
                s.execute("INSERT INTO mesas (nombre, estado) VALUES ('Mesa 4', 'LIBRE')");
            }
        } catch(Exception e) {}
    }

    // --- MÉTODOS DE LECTURA ---
    public static List<Mesa> obtenerMesas() {
        List<Mesa> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM mesas")) {
            while (rs.next()) lista.add(new Mesa(rs.getInt("id"), rs.getString("nombre"), rs.getString("estado")));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static List<Producto> obtenerProductos() {
        List<Producto> lista = new ArrayList<>();
        try (Connection conn = connect(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM productos")) {
            while (rs.next()) lista.add(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio"), "General"));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // --- NUEVOS MÉTODOS: GUARDAR Y ELIMINAR ---
    public static void guardarProducto(String nombre, double precio, int categoriaId) {
        String sql = "INSERT INTO productos(nombre, precio, categoria_id) VALUES(?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.setInt(3, categoriaId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void eliminarProducto(String nombre) {
        String sql = "DELETE FROM productos WHERE nombre = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // Método para crear una nueva mesa dinámicamente
    public static void guardarMesa(String nombre) {
        String sql = "INSERT INTO mesas (nombre, estado) VALUES (?, 'LIBRE')";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error creando mesa: " + e.getMessage());
        }
    }
}