package com.hirelance.controlador;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.ContratistaStats;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContratistaDashboardActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView textSaludoContratista, textStatProyectos, textStatPostulaciones;
    private RecyclerView recyclerProyectosContratista;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idContratistaActual;

    // RecyclerView
    private ContratistaProyectoAdapter adapter;
    private List<Proyecto> listaProyectos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contratista_dashboard);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();
        idContratistaActual = sessionManager.getUserId();

        if (tokenActual == null || idContratistaActual == -1) {
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser(); // Si falla, lo mandamos al login
            return;
        }

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar RecyclerView
        configurarRecyclerView();

        // 5. Cargar todos los datos (Estadísticas y Proyectos)
        cargarEstadisticas();
        cargarProyectosRecientes();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarContratista);
        progressBar = findViewById(R.id.progressBarContratista);
        textSaludoContratista = findViewById(R.id.textSaludoContratista);
        textStatProyectos = findViewById(R.id.textStatProyectos);
        textStatPostulaciones = findViewById(R.id.textStatPostulaciones);
        recyclerProyectosContratista = findViewById(R.id.recyclerProyectosContratista);

        // (Asegúrate de que tu FAB en el XML tenga el id 'fabPublicarProyecto')
        findViewById(R.id.fabPublicarProyecto).setOnClickListener(v -> {
            Intent intent = new Intent(ContratistaDashboardActivity.this, PublicarProyectoActivity.class);
            startActivity(intent);
        });
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    // --- 6. INFLAR EL MENÚ DEL CONTRATISTA ---
    // (Necesitaremos crear 'contratista_menu.xml' después)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.contratista_menu, menu);
        // Por ahora, usamos el mismo menú de estudiante como placeholder
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_perfil) {
            // (WIP) Abrir el PerfilContratistaActivity
            Toast.makeText(this, "WIP: Abriendo perfil contratista", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_salir) {
            sessionManager.logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void configurarRecyclerView() {
        adapter = new ContratistaProyectoAdapter(listaProyectos, this);
        recyclerProyectosContratista.setLayoutManager(new LinearLayoutManager(this));
        recyclerProyectosContratista.setAdapter(adapter);
    }

    private void cargarEstadisticas() {
        progressBar.setVisibility(View.VISIBLE);
        Call<ContratistaStats> call = apiService.getContratistaStats(tokenActual, idContratistaActual);
        call.enqueue(new Callback<ContratistaStats>() {
            @Override
            public void onResponse(Call<ContratistaStats> call, Response<ContratistaStats> response) {
                progressBar.setVisibility(View.GONE); // Ocultar solo si ambas llamadas terminan
                if (response.isSuccessful() && response.body() != null) {
                    ContratistaStats stats = response.body();
                    textStatProyectos.setText(String.valueOf(stats.getProyectosTotales()));
                    textStatPostulaciones.setText(String.valueOf(stats.getPostulacionesTotales()));
                    // (WIP: Falta el TextView de "Pendientes de Revisión" en el XML)
                } else {
                    Toast.makeText(ContratistaDashboardActivity.this, "Error al cargar estadísticas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ContratistaStats> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ContratistaDashboardActivity.this, "Error de red (Stats): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProyectosRecientes() {
        Call<List<Proyecto>> call = apiService.getMisProyectosContratista(tokenActual, idContratistaActual);
        call.enqueue(new Callback<List<Proyecto>>() {
            @Override
            public void onResponse(Call<List<Proyecto>> call, Response<List<Proyecto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setProyectos(response.body());
                } else {
                    Toast.makeText(ContratistaDashboardActivity.this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Proyecto>> call, Throwable t) {
                Toast.makeText(ContratistaDashboardActivity.this, "Error de red (Proyectos): " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}