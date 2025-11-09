package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hirelance.util.SessionManager; // <-- 1. IMPORTAR


import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Vistas
    private RecyclerView recyclerViewProyectos;
    private ProgressBar progressBarMain;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Red y session
    private ApiService apiService;
    private ProyectoAdapter proyectoAdapter;

    private SessionManager sessionManager; // <-- 2. DECLARAR SESSION MANAGER
    private String tokenActual;            // <-- 3. DECLARAR VARIABLE PARA EL TOKEN


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar API Service
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar RecyclerView
        configurarRecyclerView();

        // 4. Configurar Swipe-to-Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Llama a cargarProyectos() pero no muestra el ProgressBar central
            cargarProyectos(false);
        });

        // 5. Cargar los datos por primera vez
        cargarProyectos(true);
    }

    private void vincularVistas() {
        // (La Toolbar la vinculamos si queremos añadirle menú, si no, no es necesario)
        recyclerViewProyectos = findViewById(R.id.recyclerViewProyectos);
        progressBarMain = findViewById(R.id.progressBarMain);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void configurarRecyclerView() {
        // Usamos un LayoutManager lineal (lista vertical)
        recyclerViewProyectos.setLayoutManager(new LinearLayoutManager(this));
        // Creamos el adaptador (vacío al inicio)
        proyectoAdapter = new ProyectoAdapter(this);
        // Asignamos el adaptador al RecyclerView
        recyclerViewProyectos.setAdapter(proyectoAdapter);
    }

    /**
     * Llama a la API para obtener la lista de proyectos.
     * @param mostrarProgressBarCentral true si debe mostrar el ProgressBar del centro,
     * false si solo debe mostrar el de SwipeRefresh.
     */
    private void cargarProyectos(boolean mostrarProgressBarCentral) {
        // Mostrar el indicador de carga apropiado
        if (mostrarProgressBarCentral) {
            progressBarMain.setVisibility(View.VISIBLE);
        } else {
            // El de SwipeRefresh se muestra automáticamente
        }

        // Hacemos la llamada a la API (definida en ApiService)
        Call<List<Proyecto>> call = apiService.getProyectos();

        call.enqueue(new Callback<List<Proyecto>>() {
            @Override
            public void onResponse(Call<List<Proyecto>> call, Response<List<Proyecto>> response) {
                // Ocultamos ambos indicadores de carga
                progressBarMain.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito! Tenemos la lista.
                    List<Proyecto> proyectos = response.body();

                    // (Aquí podríamos verificar si la lista está vacía y mostrar un mensaje)

                    // Enviamos la lista al adaptador
                    proyectoAdapter.setProyectos(proyectos);

                } else {
                    // Error del servidor (ej. 401 No Autorizado, 500 Error Interno)
                    Toast.makeText(MainActivity.this, "Error al cargar los proyectos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Proyecto>> call, Throwable t) {
                // Error de red (sin conexión, servidor caído)
                progressBarMain.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }
}