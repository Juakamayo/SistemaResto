package org.example;

public class Mesa {
    private int id;
    private String nombre;
    private String estado; // "LIBRE", "OCUPADA"

    public Mesa(int id, String nombre, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
    }

    // Getters necesarios para leer los datos
    public String getNombre() { return nombre; }
    public String getEstado() { return estado; }
    public int getId() { return id; }
}