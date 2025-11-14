package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * POJO para recibir la respuesta de la API con los detalles
 * combinados de un perfil de estudiante y su postulación específica.
 */
public class DetallePostulante {

    @SerializedName("perfil")
    private PerfilEstudiante perfil;

    @SerializedName("postulacion")
    private Postulacion postulacion;

    // --- Getters ---

    public PerfilEstudiante getPerfil() {
        return perfil;
    }

    public Postulacion getPostulacion() {
        return postulacion;
    }
}