package com.example.tidyup;

public class Tarea {

    // 1. Atributos (Las características que tiene cada tarea)
    private String nombre;
    private String fechaOHora;
    private String usuario;
    private boolean completada;

    // 2. Constructor (El método que usamos para button2 una tarea nueva)
    public Tarea(String nombre, String fechaOHora, String usuario, boolean completada) {
        this.nombre = nombre;
        this.fechaOHora = fechaOHora;
        this.usuario = usuario;
        this.completada = completada;
    }

    // 3. Getters y Setters (Métodos para leer o modificar los datos después de creados)

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaOHora() {
        return fechaOHora;
    }

    public void setFechaOHora(String fechaOHora) {
        this.fechaOHora = fechaOHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}