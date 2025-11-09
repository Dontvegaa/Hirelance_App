package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo POJO para la tabla 'categorias'.
 * Representa una categoría de servicio.
 */
public class Categoria {

    @SerializedName("id_categoria")
    private int idCategoria;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("descripcion")
    private String descripcion;

    // --- Constructor (puede ser útil para crear nuevos) ---
    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // --- Getters y Setters ---

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}