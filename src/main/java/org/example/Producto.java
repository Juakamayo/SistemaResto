package org.example;

public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private String categoria;

    public Producto(int id, String nombre, double precio, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
    }

    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getCategoria() { return categoria; }

    @Override
    public String toString() {
        return nombre; // Esto es lo que se mostrará en las listas simples
    }
}