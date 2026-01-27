package com.example.tidyup; // <--- CAMBIA ESTO POR TU PAQUETE REAL SI ES DISTINTO

public class Tarea {
    private String nombre;
    private String hora;

    public Tarea(String nombre, String hora) {
        this.nombre = nombre;
        this.hora = hora;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHora() {
        return hora;
    }
}