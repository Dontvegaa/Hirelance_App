package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException; // <--- 1. IMPORTA ESTO
import com.hirelance.modelo.Usuario; // <-- Asegúrate de importar Usuario
import java.io.IOException; // <-- Asegúrate de importar IOException

import com.hirelance.util.SessionManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hirelance.R;
import com.hirelance.modelo.LoginResponse;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // Vistas (elementos del XML)
    private TextInputLayout layoutInputCorreo, layoutInputContrasena;
    private TextInputEditText editCorreo, editContrasena;
    private MaterialButton botonLogin;
    private TextView textIrARegistro;
    private ProgressBar progressBarLogin;

    // Conexión a la API
    private ApiService apiService;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicializar el servicio de la API
        // Obtenemos el cliente de Retrofit (de la Parte 2) y creamos el servicio
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. ¡AÑADIR! Inicializar SessionManager
        sessionManager = new SessionManager(getApplicationContext());

        // --- LÓGICA DE REDIRECCIÓN EN EL INICIO ---
        // Si el usuario ya está logueado, lo mandamos al dashboard correcto
        if (sessionManager.isLoggedIn()) {
            String tipoUsuario = sessionManager.getUserType();

            // Comprobamos el tipo de usuario guardado
            if ("contratista".equals(tipoUsuario)) {
                irAContratistaDashboard();
            } else {
                // "estudiante" o cualquier otro caso
                irAMainActivity();
            }
            finish(); // Cierra LoginActivity
            return; // Detenemos la ejecución de onCreate
        }

        // 4. Vincular las Vistas del XML
        vincularVistas();

        // 5. Configurar los Listeners (eventos de click)
        configurarListeners();
    }

    private void vincularVistas() {
        layoutInputCorreo = findViewById(R.id.layoutInputCorreo);
        layoutInputContrasena = findViewById(R.id.layoutInputContrasena);
        editCorreo = findViewById(R.id.editCorreo);
        editContrasena = findViewById(R.id.editContrasena);
        botonLogin = findViewById(R.id.botonLogin);
        textIrARegistro = findViewById(R.id.textIrARegistro);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    private void configurarListeners() {
        // Click en el botón de "Iniciar Sesión"
        botonLogin.setOnClickListener(v -> {
            // v es la "vista" (el botón) que fue clickeada
            intentarLogin();
        });

        // Click en el texto "Regístrate aquí"
        textIrARegistro.setOnClickListener(v -> {
            // Creamos una intención (Intent) para abrir la pantalla de Registro
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            // (RegisterActivity.java la crearemos en la siguiente parte)
        });
    }

    /**
     * Valida los campos e inicia la llamada a la API para el login.
     */
    private void intentarLogin() {
        // Limpiamos errores previos
        layoutInputCorreo.setError(null);
        layoutInputContrasena.setError(null);

        // 1. Obtener datos de los campos
        String correo = editCorreo.getText().toString().trim();
        String contrasena = editContrasena.getText().toString().trim();

        // 2. Validar campos
        if (!validarCampos(correo, contrasena)) {
            return; // Si la validación falla, no continuamos
        }

        // 3. Mostrar el ProgressBar y ocultar el botón (para evitar doble click)
        progressBarLogin.setVisibility(View.VISIBLE);
        botonLogin.setEnabled(false);

        // 4. Crear y ejecutar la llamada a la API (definida en ApiService)
        Call<LoginResponse> call = apiService.login(correo, contrasena);

        // 5. Ejecutar la llamada en segundo plano (asíncrono)
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // La llamada terminó. Ocultamos el ProgressBar y reactivamos el botón.
                progressBarLogin.setVisibility(View.GONE);
                botonLogin.setEnabled(true);

                // 6. Analizar la respuesta
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito! El servidor nos devolvió 200 (OK) y un cuerpo
                    LoginResponse loginResponse = response.body();
                    Usuario usuario = loginResponse.getUsuario(); // Obtenemos el usuario

                    /// 1. Obtener todos los datos de la sesión
                    String token = loginResponse.getToken();
                    int userId = usuario.getIdUsuario();
                    String userType = usuario.getTipo(); // <-- ¡NUEVO! Leemos el tipo

                    // 2. Guardar la sesión COMPLETA
                    sessionManager.saveSession(token, userId, userType);

                    // 3. Mostrar bienvenida
                    String bienvenida = getString(R.string.bienvenida_usuario, usuario.getNombre());
                    Toast.makeText(LoginActivity.this, bienvenida, Toast.LENGTH_LONG).show();

                    // 4. --- ¡LÓGICA DE REDIRECCIÓN! ---
                    if ("contratista".equals(userType)) {
                        irAContratistaDashboard();
                    } else {
                        // Por defecto, (estudiante o admin) va al dashboard de estudiante
                        irAMainActivity();
                    }

                } else {
                    // Error del servidor (401, etc.)
                    String errorMsg = "Error al iniciar sesión.";
                    if(response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // --- 2. ¡ESTE ES EL CAMBIO! ---
                progressBarLogin.setVisibility(View.GONE);
                botonLogin.setEnabled(true);

                // Obtenemos el mensaje de la excepción real
                String errorMessage = t.getMessage();

                // Lo mostramos en el Logcat para verlo completo
                Log.e("LOGIN_ERROR", "onFailure: " + errorMessage, t);

                // Lo mostramos en un Toast largo
                Toast.makeText(LoginActivity.this, "Error (onFailure): " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAContratistaDashboard() {
        // --- ¡CORREGIDO! ---
        // Ya no es un Toast, ahora es un Intent real
        Intent intent = new Intent(LoginActivity.this, ContratistaDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Método centralizado para ir a MainActivity y cerrar la actual.
     */
    private void irAMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cerramos LoginActivity para que no pueda volver con "atrás"
    }

    /**
     * Valida que los campos de login no estén vacíos y el correo sea válido.
     */
    private boolean validarCampos(String correo, String contrasena) {
        if (TextUtils.isEmpty(correo)) {
            layoutInputCorreo.setError(getString(R.string.error_campo_vacio));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            layoutInputCorreo.setError(getString(R.string.error_corre_invalido));
            return false;
        }

        if (TextUtils.isEmpty(contrasena)) {
            layoutInputContrasena.setError(getString(R.string.error_campo_vacio));
            return false;
        }

        return true;
    }
}