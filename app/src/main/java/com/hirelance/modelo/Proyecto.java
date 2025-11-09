package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;
// Importamos java.util.Date para manejar las fechas
import java.util.Date;

/**
 * Modelo POJO para la tabla 'proyectos'.
 * Esta es una de las clases centrales de la app.
 */
public class Proyecto {

    @SerializedName("id_proyecto")
    private int idProyecto;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("presupuesto")
    private double presupuesto; // SQL DECIMAL se mapea bien a double

    @SerializedName("fecha_publicacion")
    private Date fechaPublicacion; // SQL TIMESTAMP se mapea a java.util.Date

    @SerializedName("fecha_limite")
    private Date fechaLimite; // SQL DATE también se mapea a java.util.Date

    @SerializedName("estado")
    private String estado; // El ENUM de SQL se maneja como String

    // --- Objetos Anidados ---
    // En lugar de solo tener 'id_contratista' e 'id_categoria',
    // es una BUENA PRÁCTICA que la API nos envíe los objetos completos.
    // Esto evita que la app tenga que hacer 3 llamadas para mostrar un proyecto.

    /**
     * El contratista (Usuario) que publicó el proyecto.
     * La API debería anidar este objeto.
     */
    @SerializedName("contratista") // Asumimos que la API lo llama "contratista"
    private Usuario contratista;

    /**
     * La categoría a la que pertenece el proyecto.
     * La API debería anidar este objeto.
     */
    @SerializedName("categoria") // Asumimos que la API lo llama "categoria"
    private Categoria categoria;


    // --- Getters y Setters ---

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
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

    public double getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(double presupuesto) {
        this.presupuesto = presupuesto;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Date getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(Date fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getContratista() {
        return contratista;
    }

    public void setContratista(Usuario contratista) {
        this.contratista = contratista;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}