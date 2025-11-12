package com.hirelance.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu; // <-- ¡NUEVO IMPORT!
import android.view.MenuItem; // <-- ¡NUEVO IMPORT!
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull; // <-- ¡NUEVO IMPORT!
import androidx.appcompat.widget.Toolbar; // <-- ¡NUEVO IMPORT!
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hirelance.R;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager; // <-- 1. IMPORTAR

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Vistas
    private RecyclerView recyclerProyectos;
    private ProgressBar progressBarMain;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbarMain; // <-- ¡NUEVA VISTA!

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager; // <-- 2. DECLARAR SESSION MANAGER
    private String tokenActual;            // <-- 3. DECLARAR VARIABLE PARA EL TOKEN

    // RecyclerView
    private ProyectoAdapter proyectoAdapter;
    private List<Proyecto> listaDeProyectos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 4. CONFIGURAR SESIÓN ---
        sessionManager = new SessionManager(getApplicationContext());
        tokenActual = sessionManager.getToken();

        // Si no hay token, no debería estar aquí. Lo enviamos al Login.
        if (tokenActual == null) {
            irALogin();
            return; // Detenemos la ejecución de onCreate
        }
        // --- FIN DE CONFIGURACIÓN DE SESIÓN ---

        // Configurar API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // Vincular Vistas
        vincularVistas();

        // --- ¡NUEVO! Configurar Toolbar ---
        setSupportActionBar(toolbarMain);

        // Configurar RecyclerView
        configurarRecyclerView();

        // Configurar SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // El listener se activa al "jalar"
            cargarProyectos();
        });

        // Cargar los datos por primera vez
        cargarProyectos();
    }

    private void vincularVistas() {
        toolbarMain = findViewById(R.id.toolbarMain); // <-- ¡NUEVO VÍNCULO!
        recyclerProyectos = findViewById(R.id.recyclerViewProyectos);
        progressBarMain = findViewById(R.id.progressBarMain);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    // --- ¡NUEVO! METODO PARA CREAR EL MENÚ ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú (añade los ítems a la barra de acción)
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    // --- ¡NUEVO! METODO PARA MANEJAR CLICS EN EL MENÚ ---
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Maneja los clics en los ítems de la barra de acción
        int id = item.getItemId();

        if (id == R.id.menu_perfil) {
            // El usuario hizo clic en "Mi Perfil"
            // (Asumimos que el usuario es Estudiante por ahora)
            // (En una app real, aquí comprobaríamos sessionManager.getUserType())
            Intent intent = new Intent(this, PerfilEstudianteActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_postulaciones) {
            // El usuario hizo clic en "Mis Postulaciones"
            Intent intent = new Intent(this, MisPostulacionesActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.menu_salir) {
            // El usuario hizo clic en "Cerrar Sesión"
            irALogin(); // El método irALogin ya limpia la sesión
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void configurarRecyclerView() {
        proyectoAdapter = new ProyectoAdapter(listaDeProyectos, this);
        recyclerProyectos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProyectos.setAdapter(proyectoAdapter);
    }

    /**
     * Llama a la API para obtener la lista de proyectos.
     */
    private void cargarProyectos() {
        // Mostramos el indicador de carga
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBarMain.setVisibility(View.VISIBLE);
        }

        // --- 5. PASAR EL TOKEN A LA LLAMADA (ESTA LÍNEA ARREGLA TU ERROR) ---
        Call<List<Proyecto>> call = apiService.getProyectos(tokenActual);

        call.enqueue(new Callback<List<Proyecto>>() {
            @Override
            public void onResponse(Call<List<Proyecto>> call, Response<List<Proyecto>> response) {
                // Ocultamos ambos indicadores de carga
                progressBarMain.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito!
                    listaDeProyectos.clear();
                    listaDeProyectos.addAll(response.body());
                    proyectoAdapter.notifyDataSetChanged(); // Notifica al adapter que los datos cambiaron
                } else if (response.code() == 401) {
                    // Error 401 = Token inválido o expirado
                    Toast.makeText(MainActivity.this, "Tu sesión ha expirado.", Toast.LENGTH_SHORT).show();
                    irALogin();
                } else {
                    // Otro error
                    Toast.makeText(MainActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Proyecto>> call, Throwable t) {
                // Error de red
                progressBarMain.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Método para enviar al usuario a LoginActivity y limpiar la sesión.
     */
    private void irALogin() {
        sessionManager.clearSession(); // Limpiamos la sesión guardada
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Flags para limpiar el historial y que no pueda volver a MainActivity con "atrás"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Cerramos esta actividad
    }
}