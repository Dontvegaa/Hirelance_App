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
    @POST("auth/login") // Usando la ruta de tu archivo original
    Call<LoginResponse> login(
            @Field("correo") String correo,
            @Field("contrasena") String contrasena
    );

    /**
     * Petición para registrar un nuevo usuario.
     * (Llamado por RegisterActivity)
     */
    @POST("auth/register") // Usando la ruta de tu archivo original
    Call<Usuario> register(@Body Usuario usuario);


    // ======================================================
    // === 2. ENDPOINTS PROTEGIDOS (Requieren Token) ===
    // ======================================================

    /**
     * Obtiene la lista de todos los proyectos.
     * (Llamado por MainActivity)
     * @param token Token de autorización (Ej: "Bearer ...")
     */
    @GET("proyectos")
    Call<List<Proyecto>> getProyectos(@Header("Authorization") String token);

    /**
     * Obtiene un proyecto específico por su ID.
     * (Llamado por DetalleProyectoActivity)
     * @param token Token de autorización
     * @param idProyecto El ID del proyecto a cargar
     */
    @GET("proyectos/{id}") // Usando la ruta de tu archivo original
    Call<Proyecto> getProyectoDetalle(
            @Header("Authorization") String token,
            @Path("id") int idProyecto
    );

    /**
     * Envía una nueva postulación a un proyecto.
     * (Llamado por PostulacionActivity)
     * @param token Token de autorización
     * @param nuevaPostulacion Objeto Postulacion con los datos del formulario.
     */
    @POST("postulacion") // Endpoint que PostulacionActivity.java espera
    Call<Postulacion> enviarPostulacion(
            @Header("Authorization") String token,
            @Body Postulacion nuevaPostulacion
    );

    // ======================================================
    // === 3. ENDPOINTS DE PERFIL (Requieren Token) ===
    // ======================================================

    /**
     * Obtiene el perfil del usuario actualmente logueado.
     * La API usa el Token para saber de quién es el perfil.
     * (Asumimos que devuelve un PerfilEstudiante por ahora)
     */
    @GET("perfil/miperfil") // <-- Ruta de ejemplo, podría ser "perfil/estudiante/yo"
    Call<PerfilEstudiante> getMiPerfil(@Header("Authorization") String token);

    /**
     * Actualiza el perfil del estudiante.
     * Envía el objeto PerfilEstudiante completo en el body.
     * Requiere el token de autorización.
     */
    @PUT("api/perfil/estudiante") // <--- 2. Añade este metodo (ajusta la URL a tu API)
    Call<PerfilEstudiante> actualizarMiPerfil(
            @Header("Authorization") String token,
            @Body PerfilEstudiante perfil
    );

    /**
     * Obtiene una lista de todas las postulaciones
     * realizadas por el estudiante autenticado.
     */
    @GET("api/estudiante/mis-postulaciones") // <--- 2. Añade este metodo (ajusta la URL a tu API)
    Call<List<Postulacion>> getMisPostulaciones(
            @Header("Authorization") String token
    );


}