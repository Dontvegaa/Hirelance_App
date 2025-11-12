package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Hacemos que sea Serializable para poder pasarla entre activities si es necesario
public class Universidad implements Serializable {

    @SerializedName("id_universidad")
    private int idUniversidad;

    @SerializedName("nombre")
    private String nombre;

    // Recibiremos el MEDIUMBLOB como un string Base64
    @SerializedName("logo")
    private String logoBase64;

    // --- Getters y Setters ---

    public int getIdUniversidad() {
        return idUniversidad;
    }

    public void setIdUniversidad(int idUniversidad) {
        this.idUniversidad = idUniversidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }
}