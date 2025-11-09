package com.hirelance.controlador;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hirelance.R;
import com.hirelance.modelo.Postulacion;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostulacionActivity extends AppCompatActivity {

    // Constante para recibir el ID del proyecto
    public static final String ID_PROYECTO = "ID_PROYECTO";

    // Vistas
    private Toolbar toolbarPostulacion;
    private ProgressBar progressBarPostulacion;
    private TextInputLayout layoutInputPropuesta, layoutInputMonto, layoutInputTiempo;
    private TextInputEditText editPropuesta, editMonto, editTiempo;
    private MaterialButton btnEnviarPostulacion;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private int idProyectoActual;
    private int idEstudianteActual;
    private String tokenActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postulacion);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. Obtener IDs (Proyecto y Estudiante)
        idProyectoActual = getIntent().getIntExtra(ID_PROYECTO, -1);
        idEstudianteActual = sessionManager.getUserId();
        tokenActual = sessionManager.getToken();

        // 3. Validar que todo esté en orden
        if (idProyectoActual == -1 || idEstudianteActual == -1 || tokenActual == null) {
            Toast.makeText(this, "Error: Sesión o proyecto no válidos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 4. Configurar Vistas
        vincularVistas();
        configurarToolbar();

        // 5. Configurar Botón de Envío
        btnEnviarPostulacion.setOnClickListener(v -> {
            intentarEnviarPostulacion();
        });
    }

    private void vincularVistas() {
        toolbarPostulacion = findViewById(R.id.toolbarPostulacion);
        progressBarPostulacion = findViewById(R.id.progressBarPostulacion);
        layoutInputPropuesta = findViewById(R.id.layoutInputPropuesta);
        layoutInputMonto = findViewById(R.id.layoutInputMonto);
        layoutInputTiempo = findViewById(R.id.layoutInputTiempo);
        editPropuesta = findViewById(R.id.editPropuesta);
        editMonto = findViewById(R.id.editMonto);
        editTiempo = findViewById(R.id.editTiempo);
        btnEnviarPostulacion = findViewById(R.id.btnEnviarPostulacion);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbarPostulacion);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Maneja el clic en el botón de "atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void intentarEnviarPostulacion() {
        // 1. Validar formulario
        if (!validarFormulario()) {
            return;
        }

        // 2. Mostrar ProgressBar y desactivar botón
        progressBarPostulacion.setVisibility(View.VISIBLE);
        btnEnviarPostulacion.setEnabled(false);

        // 3. Obtener datos
        String propuesta = editPropuesta.getText().toString().trim();
        String tiempo = editTiempo.getText().toString().trim();
        double monto = 0.0;
        try {
            monto = Double.parseDouble(editMonto.getText().toString());
        } catch (NumberFormatException e) {
            // (La validación ya debería haberlo atrapado, pero es una buena práctica)
            layoutInputMonto.setError("Monto inválido");
            return;
        }

        // 4. Crear objeto Postulacion
        Postulacion nuevaPostulacion = new Postulacion(idProyectoActual, idEstudianteActual, propuesta, monto, tiempo);

        // 5. Formatear Token (Estándar Bearer)
        String tokenFormateado = "Bearer " + tokenActual;

        // 6. Hacer la llamada a la API
        Call<Postulacion> call = apiService.enviarPostulacion(tokenFormateado, nuevaPostulacion);
        call.enqueue(new Callback<Postulacion>() {
            @Override
            public void onResponse(Call<Postulacion> call, Response<Postulacion> response) {
                progressBarPostulacion.setVisibility(View.GONE);
                btnEnviarPostulacion.setEnabled(true);

                if (response.isSuccessful()) {
                    // ¡Éxito!
                    Toast.makeText(PostulacionActivity.this, "¡Postulación enviada con éxito!", Toast.LENGTH_LONG).show();
                    // Cerramos la actividad y volvemos al detalle del proyecto
                    finish();
                } else {
                    // Error del servidor (ej. 401 No Autorizado, 400 Mal Request)
                    Toast.makeText(PostulacionActivity.this, "Error al enviar la postulación.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Postulacion> call, Throwable t) {
                // Error de red
                progressBarPostulacion.setVisibility(View.GONE);
                btnEnviarPostulacion.setEnabled(true);
                Toast.makeText(PostulacionActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validarFormulario() {
        layoutInputPropuesta.setError(null);
        layoutInputMonto.setError(null);
        layoutInputTiempo.setError(null);
        boolean esValido = true;

        if (TextUtils.isEmpty(editPropuesta.getText().toString().trim())) {
            layoutInputPropuesta.setError("La propuesta no puede estar vacía");
            esValido = false;
        }

        if (TextUtils.isEmpty(editTiempo.getText().toString().trim())) {
            layoutInputTiempo.setError("El tiempo estimado no puede estar vacío");
            esValido = false;
        }

        if (TextUtils.isEmpty(editMonto.getText().toString().trim())) {
            layoutInputMonto.setError("El monto no puede estar vacío");
            esValido = false;
        } else {
            try {
                Double.parseDouble(editMonto.getText().toString());
            } catch (NumberFormatException e) {
                layoutInputMonto.setError("Por favor, introduce un número válido");
                esValido = false;
            }
        }

        return esValido;
    }
}