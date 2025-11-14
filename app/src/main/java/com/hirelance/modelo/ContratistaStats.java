package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * POJO para recibir las estadísticas del dashboard del contratista.
 */
public class ContratistaStats {

    @SerializedName("proyectos_totales")
    private int proyectosTotales;

    @SerializedName("postulaciones_totales")
    private int postulacionesTotales;

    @SerializedName("postulaciones_pendientes")
    private int postulacionesPendientes;

    // --- Getters ---

    public int getProyectosTotales() {
        return proyectosTotales;
    }

    public int getPostulacionesTotales() {
        return postulacionesTotales;
    }

    public int getPostulacionesPendientes() {
        return postulacionesPendientes;
    }
}