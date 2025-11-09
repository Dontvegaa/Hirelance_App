package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa la respuesta esperada de la API al hacer login.
 * Contiene el objeto Usuario y el Token de autenticación.
 */
public class LoginResponse {

    @SerializedName("usuario")
    private Usuario usuario;

    @SerializedName("token")
    private String token;

    // --- Getters y Setters ---

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}