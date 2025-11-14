package com.hirelance.modelo;
import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

/**
 * Modelo POJO para la tabla 'habilidad'.
 * Representa una habilidad que un estudiante puede tener (ej: "Photoshop", "Java").
 */
public class Habilidad implements Serializable {

    @SerializedName("id_habilidad")
    private int idHabilidad;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    // --- ¡AÑADE ESTE CONSTRUCTOR VACÍO! ---
    public Habilidad() {
        // Constructor vacío requerido por GSON y para
        // crear nuevas instancias en la app.
    }

    // --- TU CONSTRUCTOR EXISTENTE ---
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