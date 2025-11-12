package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.Postulacion;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePostulacionActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView textTituloProyecto, textNombreContratista, textEstadoPostulacion;
    private TextView textPropuestaContenido, textMontoOfertado, textTiempoEstimado, textFechaPostulacion;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idPostulacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_postulacion);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        // 2. Obtener el ID de la postulación (enviado por el adapter)
        idPostulacionActual = getIntent().getIntExtra("ID_POSTULACION", -1);

        if (tokenActual == null || idPostulacionActual == -1) {
            Toast.makeText(this, "Error: Postulación o sesión no válidas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Vincular Vistas
        vincularVistas();

        // 4. Configurar Toolbar
        configurarToolbar();

        // 5. Cargar los datos
        cargarDetallePostulacion();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarDetallePostulacion);
        progressBar = findViewById(R.id.progressBarDetallePostulacion);
        textTituloProyecto = findViewById(R.id.textTituloProyecto);
        textNombreContratista = findViewById(R.id.textNombreContratista);
        textEstadoPostulacion = findViewById(R.id.textEstadoPostulacion);
        textPropuestaContenido = findViewById(R.id.textPropuestaContenido);
        textMontoOfertado = findViewById(R.id.textMontoOfertado);
        textTiempoEstimado = findViewById(R.id.textTiempoEstimado);
        textFechaPostulacion = findViewById(R.id.textFechaPostulacion);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // Maneja el clic en el botón de "atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarDetallePostulacion() {
        progressBar.setVisibility(View.VISIBLE);

        Call<Postulacion> call = apiService.getPostulacionDetalle(tokenActual, idPostulacionActual);
        call.enqueue(new Callback<Postulacion>() {
            @Override
            public void onResponse(Call<Postulacion> call, Response<Postulacion> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(DetallePostulacionActivity.this, "No se pudo cargar el detalle.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Postulacion> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetallePostulacionActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintarDatos(Postulacion postulacion) {
        // Datos del Proyecto (anidado)
        if (postulacion.getProyecto() != null) {
            textTituloProyecto.setText(postulacion.getProyecto().getTitulo());

            // Datos del Contratista (anidado dentro del proyecto)
            if (postulacion.getProyecto().getContratista() != null) {
                String nombreCompleto = "Publicado por: " +
                        postulacion.getProyecto().getContratista().getNombre() + " " +
                        postulacion.getProyecto().getContratista().getApellido();
                textNombreContratista.setText(nombreCompleto);
            }
        }

        // Datos de la Postulación
        textPropuestaContenido.setText(postulacion.getPropuesta());
        textTiempoEstimado.setText(postulacion.getTiempoEstimado());
        textFechaPostulacion.setText(postulacion.getFechaPostulacion()); // Ya es un String

        // Formatear moneda
        Locale locale = new Locale("es", "SV");
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        textMontoOfertado.setText(format.format(postulacion.getMontoOfertado()));

        // Lógica de color para el Estado
        String estado = postulacion.getEstado();
        textEstadoPostulacion.setText(estado);
        int colorResId;
        if ("aceptada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_verde;
        } else if ("rechazada".equalsIgnoreCase(estado)) {
            colorResId = R.color.hl_acento_rojo;
        } else {
            colorResId = R.color.hl_acento_naranja; // Pendiente
        }
        textEstadoPostulacion.setBackgroundColor(ContextCompat.getColor(this, colorResId));
    }
}