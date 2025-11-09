package com.hirelance.red;

import com.hirelance.modelo.LoginResponse;
import com.hirelance.modelo.Proyecto;
import com.hirelance.modelo.Usuario;
import com.hirelance.modelo.Postulacion;

import retrofit2.http.Header;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Interfaz que define todos los endpoints de la API de Hirelance.
 * Retrofit usará esta interfaz para generar el código de red.
 */
public interface ApiService {

    /**
     * Petición para iniciar sesión.
     * Envía correo y contraseña como campos de formulario.
     *
     * @param correo Correo del usuario
     * @param contrasena Contraseña del usuario
     * @return Un Call que, al tener éxito, devuelve un LoginResponse (Usuario + Token)
     */
    @FormUrlEncoded
    @POST("auth/login") // Endpoint de la API (ej: .../v1/auth/login)
    Call<LoginResponse> login(
            @Field("correo") String correo,
            @Field("contrasena") String contrasena
    );

    /**
     * Petición para registrar un nuevo usuario.
     * Envía un objeto Usuario completo en el cuerpo (body) de la petición,
     * serializado a JSON automáticamente por GSON.
     *
     * @param usuario Objeto Usuario con los datos del formulario de registro
     * @return Un Call que, al tener éxito, devuelve el Usuario recién creado (con su ID)
     */
    @POST("auth/register") // Endpoint de la API (ej: .../v1/auth/register)
    Call<Usuario> register(@Body Usuario usuario);

    // --- Aquí añadiremos más peticiones en el futuro ---
    @GET("proyectos") Call<List<Proyecto>> getProyectos();
    @GET("proyectos/{id}") Call<Proyecto> getProyectoDetalle(String tokenActual, @Path("id") int idProyecto);
    @POST("proyectos/{id}/postular") Call<Postulacion> postular(@Body Postulacion postulacion);
}