package com.hirelance.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.util.SessionManager; // <-- ¡AÑADE ESTE!

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.hirelance.R;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleProyectoActivity extends AppCompatActivity {

    // Constante para recibir el ID
    public static final String ID_PROYECTO = "ID_PROYECTO";

    // Vistas
    private Toolbar toolbarDetalle;
    private ProgressBar progressBarDetalle;
    private TextView textTituloDetalle, textPresupuestoDetalle, textContratistaDetalle;
    private TextView textCategoriaDetalle, textFechaLimiteDetalle, textDescripcionCompleta;
    private MaterialButton btnPostularse;

    // Red
    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager; // <-- AÑADIR
    private int idProyectoActual;
    private String tokenActual; // <-- AÑADIR

    // Herramientas de formato
    private Locale localeElSalvador = new Locale("es", "SV");
    private NumberFormat formatadorMoneda = NumberFormat.getCurrencyInstance(localeElSalvador);
    // private SimpleDateFormat formatadorFecha = new SimpleDateFormat("dd 'de' MMMM, yyyy", localeElSalvador);  <- ya no lo usamos


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_proyecto);

        // 1. Obtener el ID del Intent
        idProyectoActual = getIntent().getIntExtra(ID_PROYECTO, -1);

        // 2. Configurar API y Vistas
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // ¡AÑADE ESTAS LÍNEAS!
        sessionManager = new SessionManager(getApplicationContext());
        tokenActual = sessionManager.getToken();

        // ¡AÑADE ESTA VALIDACIÓN!
        if (idProyectoActual == -1 || tokenActual == null) {
            Toast.makeText(this, "Error: Proyecto o sesión no válidos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ... (tu código de vincularVistas y configurarToolbar)
        vincularVistas();
        configurarToolbar();

        // 3. Cargar los datos del proyecto
        cargarDetalleProyecto();

        // 4. Configurar botón de Postularse (WIP para Parte 8)
        btnPostularse.setOnClickListener(v -> {
            // Aquí abriremos la pantalla de postulación
            Toast.makeText(this, "WIP: Abriendo pantalla de postulación...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PostulacionActivity.class);
            intent.putExtra(PostulacionActivity.ID_PROYECTO, idProyectoActual);
            startActivity(intent);
        });
    }

    private void vincularVistas() {
        toolbarDetalle = findViewById(R.id.toolbarDetalle);
        progressBarDetalle = findViewById(R.id.progressBarDetalle);
        textTituloDetalle = findViewById(R.id.textTituloDetalle);
        textPresupuestoDetalle = findViewById(R.id.textPresupuestoDetalle);
        textContratistaDetalle = findViewById(R.id.textContratistaDetalle);
        textCategoriaDetalle = findViewById(R.id.textCategoriaDetalle);
        textFechaLimiteDetalle = findViewById(R.id.textFechaLimiteDetalle);
        textDescripcionCompleta = findViewById(R.id.textDescripcionCompleta);
        btnPostularse = findViewById(R.id.btnPostularse);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbarDetalle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // No mostramos título, el layout ya lo tiene
        }
    }

    // Maneja el clic en el botón de "atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y regresa a la anterior (MainActivity)
        return true;
    }

    private void cargarDetalleProyecto() {
        progressBarDetalle.setVisibility(View.VISIBLE);

        Call<Proyecto> call = apiService.getProyectoDetalle(tokenActual, idProyectoActual);
        call.enqueue(new Callback<Proyecto>() {
            @Override
            public void onResponse(Call<Proyecto> call, Response<Proyecto> response) {
                progressBarDetalle.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Proyecto recibido! Pintamos los datos.
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(DetalleProyectoActivity.this, "No se pudo cargar el detalle del proyecto.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Proyecto> call, Throwable t) {
                progressBarDetalle.setVisibility(View.GONE);
                Toast.makeText(DetalleProyectoActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Llena todas las vistas del layout con la información del objeto Proyecto.
     */
    private void pintarDatos(Proyecto proyecto) {
        // Datos principales
        textTituloDetalle.setText(proyecto.getTitulo());
        textDescripcionCompleta.setText(proyecto.getDescripcion());

        // Formateo de Presupuesto
        textPresupuestoDetalle.setText(formatadorMoneda.format(proyecto.getPresupuesto()));

        // Formateo de Fecha Límite
        // (La línea original 'formatadorFecha.format(...)' causaba el crash)
        if (proyecto.getFechaLimite() != null) {
            // Simplemente mostramos el String tal como viene de la API
            textFechaLimiteDetalle.setText(proyecto.getFechaLimite());
        } else {
            textFechaLimiteDetalle.setText("No especificada");
        }

        // Verificación de Categoría (objeto anidado)
        if (proyecto.getCategoria() != null) {
            textCategoriaDetalle.setText(proyecto.getCategoria().getNombre());
        } else {
            textCategoriaDetalle.setVisibility(View.GONE);
            findViewById(R.id.labelCategoria).setVisibility(View.GONE); // Oculta la etiqueta también
        }

        // Verificación de Contratista (objeto anidado)
        if (proyecto.getContratista() != null) {
            String nombreCompleto = proyecto.getContratista().getNombre() + " " + proyecto.getContratista().getApellido();
            textContratistaDetalle.setText(nombreCompleto);
        } else {
            textContratistaDetalle.setText("Anónimo");
        }

        // (Aquí podríamos ocultar el botón "Postularse" si el usuario
        // que está viendo es el mismo que publicó el proyecto)
    }
}