package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

public class PerfilContratista {

    @SerializedName("id_contratista")
    private int idContratista;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("nombre_empresa")
    private String nombreEmpresa;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("sitio_web")
    private String sitioWeb;

    @SerializedName("descripcion")
    private String descripcion;

    // Para la relación con el Usuario (ej. obtener el correo directamente)
    @SerializedName("usuario")
    private Usuario usuario;

    // Constructor vacío requerido por Gson
    public PerfilContratista() {
    }

    // Getters y Setters
    public int getIdContratista() {
        return idContratista;
    }

    public void setIdContratista(int idContratista) {
        this.idContratista = idContratista;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}