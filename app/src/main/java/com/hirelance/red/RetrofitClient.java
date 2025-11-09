package com.hirelance.red;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Patrón Singleton para gestionar una única instancia de Retrofit.
 * Esto optimiza el rendimiento al reutilizar el cliente de red.
 */
public class RetrofitClient {

    // IMPORTANTE: Reemplaza esta URL con la URL de tu API
    // Debe terminar en /
    private static final String BASE_URL = "http://192.168.0.3/hirelance_api/";

    private static Retrofit retrofit = null;

    /**
     * Obtiene la instancia única de Retrofit.
     * Si no existe, la crea.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Usa GSON para convertir JSON
                    .build();
        }
        return retrofit;
    }
}