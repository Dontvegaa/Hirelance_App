package com.hirelance.modelo;

import com.google.gson.annotations.SerializedName;

public class RegisterEstudianteDTO {

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

    // --- Campos de 'perfil_estudiante' (¡CORREGIDOS!) ---
    @SerializedName("carrera")
    private String carrera;

    @SerializedName("anio_carrera") // <-- CORREGIDO a snake_case
    private int anio_carrera;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("portafolio_url") // <-- CORREGIDO a snake_case
    private String portafolio_url;

    @SerializedName("foto_perfil") // <-- CORREGIDO a snake_case
    private String foto_perfil; // El string Base64

    // --- Campos para 'habilidad_estudiante' ---
    @SerializedName("habilidades")
    private String habilidades;

    // --- Getters y Setters (¡Actualizados!) ---

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    // --- GETTER Y SETTER CORREGIDOS ---
    public int getAnio_carrera() { return anio_carrera; }
    public void setAnio_carrera(int anio_carrera) { this.anio_carrera = anio_carrera; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // --- GETTER Y SETTER CORREGIDOS ---
    public String getPortafolio_url() { return portafolio_url; }
    public void setPortafolio_url(String portafolio_url) { this.portafolio_url = portafolio_url; }

    // --- GETTER Y SETTER CORREGIDOS ---
    public String getFoto_perfil() { return foto_perfil; }
    public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

    public String getHabilidades() { return habilidades; }
    public void setHabilidades(String habilidades) { this.habilidades = habilidades; }
}