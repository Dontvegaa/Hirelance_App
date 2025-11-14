package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para enviar todos los datos del formulario de registro de Contratista.
 */
public class RegisterContratistaDTO {

    // --- Campos de 'usuarios' ---
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("apellido")
    private String apellido;
    @SerializedName("correo")
    private String correo;
    @SerializedName("contrasena")
    private String contrasena;
    @SerializedName("dui")
    private String dui;
    @SerializedName("telefono")
    private String telefono;

    // --- Campos de 'perfil_contratista' ---
    @SerializedName("empresa")
    private String empresa;
    @SerializedName("ubicacion")
    private String ubicacion;
    @SerializedName("sitio_web")
    private String sitioWeb;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("logo_empresa")
    private String logoBase64; // El string Base64 para el MEDIUMBLOB

    // --- Getters y Setters ---
    // (Puedes generarlos automáticamente en Android Studio: Alt + Insert)

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setDui(String dui) { this.dui = dui; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setLogoBase64(String logoBase64) { this.logoBase64 = logoBase64; }
}