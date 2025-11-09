package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo POJO para la tabla 'habilidad'.
 * Representa una habilidad que un estudiante puede tener (ej: "Photoshop", "Java").
 */
public class Habilidad {

    @SerializedName("id_habilidad")
    private int idHabilidad;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    // --- Constructor ---
    public Habilidad(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---

    public int getIdHabilidad() {
        return idHabilidad;
    }

    public void setIdHabilidad(int idHabilidad) {
        this.idHabilidad = idHabilidad;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}