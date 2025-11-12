package com.hirelance.modelo;
import java.io.Serializable; // <--- 1. Importa esto
import com.google.gson.annotations.SerializedName;
import java.util.List; // Para la lista de habilidades

/**
 * Modelo POJO para la tabla 'perfil_estudiante'.
 * Contiene la información extendida de un usuario 'estudiante'.
 */
public class PerfilEstudiante implements Serializable {

    @SerializedName("id_usuario")
    private int idUsuario;
    @SerializedName("id_perfil")
    private int idPerfil;

    @SerializedName("carrera")
    private String carrera;

    @SerializedName("anio_carrera")
    private int anioCarrera;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("portafolio_url")
    private String portafolioUrl;

    @SerializedName("universidades")
    private List<Universidad> universidades;

    @SerializedName("foto_perfil")
    private String fotoPerfil;

    // --- Objetos Anidados ---

    /**
     * El objeto Usuario principal al que pertenece este perfil.
     * Asumimos que la API lo anidará.
     */
    @SerializedName("usuario")
    private Usuario usuario;

    /**
     * Lista de habilidades del estudiante.
     * La API debería construir esta lista a partir de la tabla
     * 'habilidad_estudiante' (la tabla N-M).
     */
    @SerializedName("habilidades")
    private List<Habilidad> habilidades;

    // (Podríamos añadir List<Universidad> de la misma forma)


    // --- Getters y Setters ---

    public int getIdUsuario() {
        return idUsuario;
    }

    public List<Universidad> getUniversidades() {
        return universidades;
    }

    public void setUniversidades(List<Universidad> universidades) {
        this.universidades = universidades;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public int getAnioCarrera() {
        return anioCarrera;
    }

    public void setAnioCarrera(int anioCarrera) {
        this.anioCarrera = anioCarrera;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPortafolioUrl() {
        return portafolioUrl;
    }

    public void setPortafolioUrl(String portafolioUrl) {
        this.portafolioUrl = portafolioUrl;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<Habilidad> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<Habilidad> habilidades) {
        this.habilidades = habilidades;
    }
}