package com.hirelance.red;

// Imports de Modelos
import com.hirelance.modelo.Categoria;
import com.hirelance.modelo.DetallePostulante;
import com.hirelance.modelo.LoginResponse;
import com.hirelance.modelo.Postulacion; // <-- Importado
import com.hirelance.modelo.Proyecto;
import com.hirelance.modelo.Universidad;
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
import com.hirelance.modelo.ContratistaStats; // <-- 1. IMPORTA EL NUEVO POJO
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
     * Petición para registrar un nuevo ESTUDIANTE.
     */
    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
    // Volvemos a poner Call<Usuario>
    @POST("registerEstudiante.php")
    Call<Usuario> registerEstudiante(@Body RegisterEstudianteDTO dto); // <-- CAMBIADO DE VUELTA A Usuario

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
    @GET("getProyectoDetalle.php")
    Call<Proyecto> getProyectoDetalle(
            @Header("Authorization") String token,
            @Query("id") int idProyecto,
            @Query("id_usuario") int idUsuario // <-- AÑADE ESTO
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

    /**
     * Obtiene los detalles completos de UNA postulación,
     * incluyendo el Proyecto y el Contratista anidados.
     */
    @GET("getPostulacionDetalle.php")
    Call<Postulacion> getPostulacionDetalle(
            @Header("Authorization") String token,
            @Query("id_postulacion") int idPostulacion
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

    /**
     * Obtiene las estadísticas (tarjetas) para el dashboard del contratista.
     */
    @GET("getContratistaStats.php")
    Call<ContratistaStats> getContratistaStats(
            @Header("Authorization") String token,
            @Query("id_contratista") int idContratista
    );

    /**
     * Obtiene los proyectos recientes publicados por el contratista.
     */
    @GET("getMisProyectosContratista.php")
    Call<List<Proyecto>> getMisProyectosContratista(
            @Header("Authorization") String token,
            @Query("id_contratista") int idContratista
    );

    /**
     * Obtiene la lista de postulaciones (y los perfiles de los estudiantes)
     * para un proyecto específico.
     */
    @GET("getPostulacionesPorProyecto.php")
    Call<List<Postulacion>> getPostulacionesPorProyecto(
            @Header("Authorization") String token,
            @Query("id_proyecto") int idProyecto
    );

    /**
     * Obtiene el perfil completo de un estudiante Y los detalles
     * de su postulación específica.
     */
    @GET("getDetallePostulante.php")
    Call<DetallePostulante> getDetallePostulante(
            @Header("Authorization") String token,
            @Query("id_postulacion") int idPostulacion
    );

    /**
     * Acepta o rechaza una postulación.
     */
    @FormUrlEncoded
    @POST("actualizarEstadoPostulacion.php")
    Call<Void> actualizarEstadoPostulacion( // Usamos Call<Void> porque no esperamos respuesta
                                            @Header("Authorization") String token,
                                            @Field("id_postulacion") int idPostulacion,
                                            @Field("nuevo_estado") String nuevoEstado // "aceptada" o "rechazada"
    );

    /**
     * Obtiene la lista de todas las categorías de proyectos
     * para poblar el spinner.
     */
    @GET("getCategorias.php")
    Call<List<Categoria>> getCategorias(
            @Header("Authorization") String token
    );

    /**
     * Publica un nuevo proyecto.
     * Envía el objeto Proyecto como un JSON.
     */
    @POST("publicarProyecto.php")
    Call<Proyecto> publicarProyecto( // Devuelve el proyecto creado
                                     @Header("Authorization") String token,
                                     @Body Proyecto nuevoProyecto
    );

    /**
     * Obtiene la lista de TODAS las universidades
     * para poblar el spinner de "Añadir Educación".
     */
    @GET("getUniversidades.php")
    Call<List<Universidad>> getUniversidades(
            @Header("Authorization") String token
    );



}