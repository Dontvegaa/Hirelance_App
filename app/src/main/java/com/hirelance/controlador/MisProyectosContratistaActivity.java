package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisProyectosContratistaActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private RecyclerView recyclerMisProyectos;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textEmptyState;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idContratistaActual;

    // RecyclerView (Reutilizamos el adaptador del Dashboard)
    private ContratistaProyectoAdapter adapter;
    private List<Proyecto> listaProyectos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_proyectos_contratista);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();
        idContratistaActual = sessionManager.getUserId();

        if (tokenActual == null || idContratistaActual == -1) {
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser();
            return;
        }

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar RecyclerView
        configurarRecyclerView();

        // 5. Configurar SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this::cargarMisProyectos);

        // 6. Cargar los datos por primera vez
        cargarMisProyectos();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarMisProyectos);
        recyclerMisProyectos = findViewById(R.id.recyclerMisProyectos);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutMisProyectos);
        progressBar = findViewById(R.id.progressBarMisProyectos);
        textEmptyState = findViewById(R.id.textEmptyStateProyectos);
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

    private void configurarRecyclerView() {
        adapter = new ContratistaProyectoAdapter(listaProyectos, this);
        recyclerMisProyectos.setLayoutManager(new LinearLayoutManager(this));
        recyclerMisProyectos.setAdapter(adapter);
    }

    private void cargarMisProyectos() {
        // Mostramos el indicador de carga apropiado
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        textEmptyState.setVisibility(View.GONE);
        recyclerMisProyectos.setVisibility(View.GONE);

        // Usamos el mismo endpoint que el dashboard
        Call<List<Proyecto>> call = apiService.getMisProyectosContratista(tokenActual, idContratistaActual);

        call.enqueue(new Callback<List<Proyecto>>() {
            @Override
            public void onResponse(Call<List<Proyecto>> call, Response<List<Proyecto>> response) {
                // Ocultamos ambos indicadores de carga
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        textEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        recyclerMisProyectos.setVisibility(View.VISIBLE);
                        adapter.setProyectos(response.body());
                    }
                } else {
                    Toast.makeText(MisProyectosContratistaActivity.this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show();
                    textEmptyState.setVisibility(View.VISIBLE); // Mostrar "empty" si hay error
                }
            }

            @Override
            public void onFailure(Call<List<Proyecto>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MisProyectosContratistaActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                textEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}