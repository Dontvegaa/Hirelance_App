package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Modelo POJO para la tabla 'postulaciones'.
 */
public class Postulacion {

    @SerializedName("id_postulacion")
    private int idPostulacion;

    @SerializedName("id_proyecto")
    private int idProyecto;

    @SerializedName("id_estudiante")
    private int idEstudiante;

    @SerializedName("propuesta")
    private String propuesta;

    @SerializedName("monto_ofertado")
    private double montoOfertado;

    @SerializedName("tiempo_estimado")
    private String tiempoEstimado;

    @SerializedName("fecha_postulacion")
    private Date fechaPostulacion;

    @SerializedName("estado")
    private String estado; // "pendiente", "aceptada", "rechazada"

    // Constructor para ENVIAR una nueva postulación a la API
    public Postulacion(int idProyecto, int idEstudiante, String propuesta, double montoOfertado, String tiempoEstimado) {
        this.idProyecto = idProyecto;
        this.idEstudiante = idEstudiante;
        this.propuesta = propuesta;
        this.montoOfertado = montoOfertado;
        this.tiempoEstimado = tiempoEstimado;
    }

    // --- Getters y Setters (puedes generarlos automáticamente) ---
    // ... (Getters y Setters para todos los campos)
}