package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils; // <--- 1. Importa TextUtils
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hirelance.R;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.red.ApiService; // <--- 2. Importa ApiService
import com.hirelance.red.RetrofitClient; // <--- 3. Importa RetrofitClient
import com.hirelance.util.SessionManager; // <--- 4. Importa SessionManager

import retrofit2.Call; // <--- 5. Importa Call, Callback y Response
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    // Constante para recibir el perfil
    public static final String PERFIL_EXTRA = "perfil_estudiante_data";

    // Vistas
    private MaterialToolbar toolbar;
    private TextInputEditText etCarrera, etAnioCarrera, etPortafolio, etDescripcion;
    private MaterialButton btnGuardarCambios;
    private ProgressBar progressBar;

    // Datos
    private PerfilEstudiante perfilActual;

    // Red y Sesión  <--- 6. Añade estas variables
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // 7. Inicializar API y Sesión
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getApplicationContext());
        tokenActual = sessionManager.getToken();

        // 1. Vincular Vistas
        vincularVistas();

        // 2. Configurar Toolbar
        configurarToolbar();

        // 3. Recibir y pintar datos
        recibirDatos();

        // 4. Configurar listeners
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarEditarPerfil);
        etCarrera = findViewById(R.id.etCarrera);
        etAnioCarrera = findViewById(R.id.etAnioCarrera);
        etPortafolio = findViewById(R.id.etPortafolio);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        progressBar = findViewById(R.id.progressBarEditarPerfil);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        // Añadir flecha de "Atrás"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Listener para la flecha de "Atrás"
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void recibirDatos() {
        // Obtenemos el objeto PerfilEstudiante que nos envió PerfilEstudianteActivity
        perfilActual = (PerfilEstudiante) getIntent().getSerializableExtra(PERFIL_EXTRA);

        if (perfilActual != null) {
            // Llenamos los campos del formulario con los datos actuales
            etCarrera.setText(perfilActual.getCarrera());
            etAnioCarrera.setText(String.valueOf(perfilActual.getAnioCarrera()));
            etPortafolio.setText(perfilActual.getPortafolioUrl());
            etDescripcion.setText(perfilActual.getDescripcion());
        } else {
            Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Valida los campos y envía la actualización a la API.
     */
    private void guardarCambios() {
        // 8. Obtener los nuevos valores de los campos
        String carrera = etCarrera.getText().toString().trim();
        String anioStr = etAnioCarrera.getText().toString().trim();
        String portafolio = etPortafolio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // 9. Validar campos
        if (TextUtils.isEmpty(carrera) || TextUtils.isEmpty(anioStr)) {
            Toast.makeText(this, "Carrera y Año son campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int anioCarrera;
        try {
            anioCarrera = Integer.parseInt(anioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 10. Actualizar el objeto perfilActual
        perfilActual.setCarrera(carrera);
        perfilActual.setAnioCarrera(anioCarrera);
        perfilActual.setPortafolioUrl(portafolio);
        perfilActual.setDescripcion(descripcion);

        // 11. Llamar a la API
        mostrarCarga(true);

        Call<PerfilEstudiante> call = apiService.actualizarMiPerfil(tokenActual, perfilActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito!
                    Toast.makeText(EditarPerfilActivity.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    // 1. ¡AQUÍ ESTÁ EL CAMBIO!
                    // Notificamos a la activity anterior que la operación fue exitosa.
                    setResult(RESULT_OK);

                    // 2. Cerramos esta activity
                    finish();
                } else {
                    Toast.makeText(EditarPerfilActivity.this, "Error al actualizar. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                mostrarCarga(false);
                Toast.makeText(EditarPerfilActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Muestra u oculta el ProgressBar y habilita/deshabilita el botón.
     */
    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardarCambios.setEnabled(!cargando);
    }


}