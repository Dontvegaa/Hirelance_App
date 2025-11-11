package com.hirelance.red;

// Imports de Modelos
import com.hirelance.modelo.LoginResponse;
import com.hirelance.modelo.Postulacion; // <-- Importado
import com.hirelance.modelo.Proyecto;
import com.hirelance.modelo.Usuario;
import com.hirelance.modelo.PerfilEstudiante; // <-- ¡NUEVO IMPORT!

// Imports de Java
import java.util.List;

// Imports de Retrofit
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header; // <-- ¡MUY IMPORTANTE! Para enviar el Token
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.hirelance.modelo.RegisterEstudianteDTO;

/**
 * Interfaz que define todos los endpoints de la API de Hirelance.
 * Esta es la versión CORREGIDA que funciona con todas las actividades (Parte 1 a 8).
 */
public interface ApiService {

    // ======================================================
    // === 1. ENDPOINTS DE AUTENTICACIÓN (Sin Token) ===
    // ======================================================

    /**
     * Petición para iniciar sesión.
     * (Llamado por LoginActivity)
     */
    @FormUrlEncoded
    // --- ¡ESTA ES LA LÍNEA CORREGIDA! ---
    @POST("login.php") // Apunta directamente a tu script
    Call<LoginResponse> login(
            @Field("correo") String correo,
            @Field("contrasena") String contrasena
    );

    /**
     * Petición para registrar un nuevo usuario.
     * (Llamado por RegisterActivity)
     */
    @POST("registerEstudiante.php") // Apunta a un nuevo script
    Call<Usuario> registerEstudiante(@Body RegisterEstudianteDTO dto);


    // ======================================================
    // === 2. ENDPOINTS PROTEGIDOS (Requieren Token) ===
    // ======================================================

    // NOTA: Todos tus otros endpoints (getProyectos, getProyectoDetalle, etc.)
    // también están incorrectos. Apuntan a URLs "bonitas" (ej. "proyectos")
    // en lugar de a los archivos PHP (ej. "getProyectos.php").

    // Por ahora, solo nos importa el login.

    /**
     * Obtiene la lista de todos los proyectos.
     * (Llamado por MainActivity)
     * @param token Token de autorización (Ej: "Bearer ...")
     */
    @GET("getProyectos.php") // Apunta al archivo .php
    Call<List<Proyecto>> getProyectos(@Header("Authorization") String token);

    /**
     * Obtiene un proyecto específico por su ID.
     * (Llamado por DetalleProyectoActivity)
     * @param token Token de autorización
     * @param idProyecto El ID del proyecto a cargar
     */
    @GET("getProyectoDetalle.php") // Apunta al nuevo archivo .php
    Call<Proyecto> getProyectoDetalle(
            @Header("Authorization") String token,
            @Query("id") int idProyecto // Envía el ID como ?id=...
    );

    /**
     * Envía una nueva postulación a un proyecto.
     * (Llamado por PostulacionActivity)
     * @param token Token de autorización
     * @param nuevaPostulacion Objeto Postulacion con los datos del formulario.
     */
    @POST("enviarPostulacion.php") // Apunta al nuevo archivo .php
    Call<Postulacion> enviarPostulacion(
            @Header("Authorization") String token,
            @Body Postulacion nuevaPostulacion // Se envía como JSON
    );

    // ======================================================
    // === 3. ENDPOINTS DE PERFIL (Requieren Token) ===
    // ======================================================

    /**
     * Obtiene el perfil del usuario actualmente logueado.
     * La API usa el Token para saber de quién es el perfil.
     * (Asumimos que devuelve un PerfilEstudiante por ahora)
     */
    @GET("getMiPerfil.php") // Apunta al nuevo archivo .php
    Call<PerfilEstudiante> getMiPerfil(
            @Header("Authorization") String token,
            @Query("id_usuario") int idUsuario // Envía el ID del usuario
    );
    /**
     * Actualiza el perfil del estudiante.
     * Envía el objeto PerfilEstudiante completo en el body.
     * Requiere el token de autorización.
     */
    @PUT("actualizarMiPerfil.php") // Apunta al nuevo archivo .php
    Call<PerfilEstudiante> actualizarMiPerfil(
            @Header("Authorization") String token,
            @Body PerfilEstudiante perfil
    );

    /**
     * Obtiene una lista de todas las postulaciones
     * realizadas por el estudiante autenticado.
     */
    @GET("getMisPostulaciones.php") // Apunta al nuevo archivo .php
    Call<List<Postulacion>> getMisPostulaciones(
            @Header("Authorization") String token,
            @Query("id_usuario") int idUsuario // Envía el ID del usuario
    );


}