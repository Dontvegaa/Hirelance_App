package com.hirelance.modelo;
import java.io.Serializable; // <--- 1. Importa esto
import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Modelo POJO para la tabla 'usuarios'.
 * Esta clase se usa para mapear los datos JSON recibidos de la API,
 * utilizando GSON para la deserialización.
 */
public class Usuario implements Serializable {

    // SerializedName mapea la clave JSON (igual que la columna de la BD)
    // al nombre del campo en Java.
    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("correo")
    private String correo;

    @SerializedName("contrasena")
    private String contrasena; // Usar solo para enviar, no para almacenar

    @SerializedName("dui")
    private String dui;

    @SerializedName("tipo")
    private String tipo; // "estudiante", "contratista", "admin"

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("fecha_registro")
    private String fechaRegistro; // <-- Cambiado de Date a String

    @SerializedName("estado")
    private String estado; // "activo", "inactivo", "baneado"

    // Constructor vacío (requerido por algunas librerías como GSON/Retrofit)
    public Usuario() {
    }

    // Constructor para crear un usuario (ej. para el registro)
    public Usuario(String nombre, String apellido, String correo, String contrasena, String dui, String tipo, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasena = contrasena;
        this.dui = dui;
        this.tipo = tipo;
        this.telefono = telefono;
    }

    // --- Getters y Setters ---

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaRegistro() { // <-- Cambiado de Date a String
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) { // <-- Cambiado de Date a String
        this.fechaRegistro = fechaRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}