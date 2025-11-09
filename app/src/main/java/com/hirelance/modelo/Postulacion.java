package com.hirelance.modelo;
import java.io.Serializable; // <--- 2. Importa Serializable (buena práctica)
import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Modelo POJO para la tabla 'postulaciones'.
 */
public class Postulacion implements Serializable {

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

    // --- 4. AÑADE EL CAMPO PARA EL OBJETO ANIDADO ---
    // GSON buscará un objeto JSON llamado "proyecto" en la respuesta
    // y lo convertirá automáticamente en un objeto Proyecto.java
    @SerializedName("proyecto")
    private Proyecto proyecto;

    // Constructor para ENVIAR una nueva postulación a la API
    public Postulacion(int idProyecto, int idEstudiante, String propuesta, double montoOfertado, String tiempoEstimado) {
        this.idProyecto = idProyecto;
        this.idEstudiante = idEstudiante;
        this.propuesta = propuesta;
        this.montoOfertado = montoOfertado;
        this.tiempoEstimado = tiempoEstimado;
    }

    // --- 5. AÑADE EL GETTER PARA EL PROYECTO ---
    public Proyecto getProyecto() {
        return proyecto;
    }

    // --- Getters existentes (ejemplos) ---

    public int getIdPostulacion() {
        return idPostulacion;
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public String getPropuesta() {
        return propuesta;
    }

    public double getMontoOfertado() {
        return montoOfertado;
    }

    public String getTiempoEstimado() {
        return tiempoEstimado;
    }

    public Date getFechaPostulacion() {
        return fechaPostulacion;
    }

    public String getEstado() {
        return estado;
    }

    // (Asegúrate de tener todos los demás getters y setters si los necesitas)
}