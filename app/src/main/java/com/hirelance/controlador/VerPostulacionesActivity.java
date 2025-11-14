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

public class VerPostulacionesActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private RecyclerView recyclerPostulantes;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idProyectoActual;

    // RecyclerView
    private PostulanteAdapter adapter;
    private List<Postulacion> listaPostulantes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_postulaciones);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        // 2. Obtener el ID del proyecto (enviado por el ContratistaProyectoAdapter)
        idProyectoActual = getIntent().getIntExtra("ID_PROYECTO", -1);

        if (tokenActual == null || idProyectoActual == -1) {
            Toast.makeText(this, "Error: Proyecto o sesión no válidos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Vincular Vistas
        vincularVistas();

        // 4. Configurar Toolbar
        configurarToolbar();

        // 5. Configurar RecyclerView
        configurarRecyclerView();

        // 6. Cargar los datos
        cargarPostulaciones();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarVerPostulaciones);
        recyclerPostulantes = findViewById(R.id.recyclerPostulantes);
        progressBar = findViewById(R.id.progressBarVerPostulaciones);
        textEmptyState = findViewById(R.id.textEmptyState);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void configurarRecyclerView() {
        adapter = new PostulanteAdapter(listaPostulantes, this);
        recyclerPostulantes.setLayoutManager(new LinearLayoutManager(this));
        recyclerPostulantes.setAdapter(adapter);
    }

    private void cargarPostulaciones() {
        progressBar.setVisibility(View.VISIBLE);

        Call<List<Postulacion>> call = apiService.getPostulacionesPorProyecto(tokenActual, idProyectoActual);
        call.enqueue(new Callback<List<Postulacion>>() {
            @Override
            public void onResponse(Call<List<Postulacion>> call, Response<List<Postulacion>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        textEmptyState.setVisibility(View.VISIBLE);
                        recyclerPostulantes.setVisibility(View.GONE);
                    } else {
                        textEmptyState.setVisibility(View.GONE);
                        recyclerPostulantes.setVisibility(View.VISIBLE);
                        adapter.setPostulaciones(response.body());
                    }
                } else {
                    Toast.makeText(VerPostulacionesActivity.this, "No se pudo cargar las postulaciones.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Postulacion>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VerPostulacionesActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}