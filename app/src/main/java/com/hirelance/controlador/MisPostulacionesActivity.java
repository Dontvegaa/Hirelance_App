package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisPostulacionesActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private RecyclerView recyclerMisPostulaciones;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;

    // RecyclerView
    private PostulacionAdapter postulacionAdapter;
    private List<Postulacion> listaPostulaciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_postulaciones);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        if (tokenActual == null) {
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar RecyclerView
        configurarRecyclerView();

        // 5. Cargar los datos
        cargarPostulaciones();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarMisPostulaciones);
        recyclerMisPostulaciones = findViewById(R.id.recyclerMisPostulaciones);
        progressBar = findViewById(R.id.progressBarMisPostulaciones);
        textEmptyState = findViewById(R.id.textEmptyState);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        // Mostrar flecha de regreso
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

    private void configurarRecyclerView() {
        // Pasamos 'this' como el Contexto al adaptador
        postulacionAdapter = new PostulacionAdapter(listaPostulaciones, this);
        recyclerMisPostulaciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerMisPostulaciones.setAdapter(postulacionAdapter);
    }

    private void cargarPostulaciones() {
        mostrarCarga(true);

        Call<List<Postulacion>> call = apiService.getMisPostulaciones(tokenActual);
        call.enqueue(new Callback<List<Postulacion>>() {
            @Override
            public void onResponse(Call<List<Postulacion>> call, Response<List<Postulacion>> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Verificamos si la lista está vacía
                    if (response.body().isEmpty()) {
                        textEmptyState.setVisibility(View.VISIBLE);
                        recyclerMisPostulaciones.setVisibility(View.GONE);
                    } else {
                        // Si hay datos, los mostramos
                        textEmptyState.setVisibility(View.GONE);
                        recyclerMisPostulaciones.setVisibility(View.VISIBLE);
                        // Actualizamos el adaptador
                        postulacionAdapter.setPostulaciones(response.body());
                    }
                } else {
                    Toast.makeText(MisPostulacionesActivity.this, "No se pudo cargar las postulaciones.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Postulacion>> call, Throwable t) {
                mostrarCarga(false);
                Toast.makeText(MisPostulacionesActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (cargando) {
            recyclerMisPostulaciones.setVisibility(View.GONE);
            textEmptyState.setVisibility(View.GONE);
        }
    }
}