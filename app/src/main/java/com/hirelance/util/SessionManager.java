package com.hirelance.util;

import android.content.Context;
import android.content.Intent; // <-- 1. IMPORTA INTENT
import android.content.SharedPreferences;

import com.hirelance.controlador.LoginActivity; // <-- 2. IMPORTA LOGINACTIVITY

/**
 * Clase de utilidad para gestionar los datos de sesión del usuario
 * (Token, ID y Tipo) usando SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "HirelanceSession";
    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type"; // <-- 3. NUEVA CLAVE
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // <-- 4. NUEVA CLAVE (Opcional pero recomendada)


    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Guarda la sesión del usuario después del login.
     * @param token El token JWT recibido de la API.
     * @param userId El ID del usuario.
     * @param userType El tipo de usuario (ej: "estudiante", "contratista").
     */
    // --- 5. MÉTODO ACTUALIZADO ---
    public void saveSession(String token, int userId, String userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType); // <-- 6. AÑADIDO
        editor.apply(); // 'apply()' es más eficiente que 'commit()'
    }

    /**
     * Obtiene el token de autenticación guardado.
     * @return El token, o null si no hay ninguno.
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Obtiene el ID del usuario logueado.
     * @return El ID del usuario, o -1 si no hay ninguno.
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Obtiene el TIPO de usuario guardado.
     * @return El tipo de usuario, o null si no hay ninguno.
     */
    // --- 7. NUEVO MÉTODO ---
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }

    /**
     * Borra todos los datos de la sesión (para el logout).
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Comprueba si el usuario está logueado.
     */
    public boolean isLoggedIn() {
        // Ahora usamos la clave booleana que es más confiable
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Limpia todos los datos de la sesión y redirige al Login.
     * (Usado en tu MainActivity.java)
     */
    // --- 8. NUEVO MÉTODO (Mejorado) ---
    public void logoutUser() {
        // 1. Limpiar todos los datos
        clearSession();

        // 2. Redirigir a LoginActivity
        Intent i = new Intent(context, LoginActivity.class);
        // Añadir flags para limpiar el historial de activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}