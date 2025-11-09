package com.hirelance.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Clase de utilidad para gestionar los datos de sesión del usuario
 * (Token y ID) usando SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "HirelanceSession";
    private static final String KEY_TOKEN = "api_token";
    private static final String KEY_USER_ID = "user_id";

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
     */
    public void saveSession(String token, int userId) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
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
     * Borra todos los datos de la sesión (para el logout).
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    /**
     * Comprueba si el usuario está logueado (si existe un token).
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}