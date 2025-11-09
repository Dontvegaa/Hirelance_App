package com.hirelance.controlador;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hirelance.R;
import com.hirelance.modelo.Habilidad;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilEstudianteActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbarPerfil;
    private ProgressBar progressBarPerfil;
    private ImageView imgFotoPerfil;
    private TextView textNombreCompleto, textCarrera, textCorreo, textDescripcionPerfil;
    private MaterialButton btnEditarPerfil;
    private RecyclerView recyclerHabilidades;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;

    // RecyclerView
    private HabilidadAdapter habilidadAdapter;
    private List<Habilidad> listaHabilidades = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_estudiante);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        if (tokenActual == null) {
            // Esto no debería pasar si llegó desde MainActivity, pero es una buena práctica
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar RecyclerView de Habilidades
        configurarRecyclerHabilidades();

        // 5. Cargar datos del perfil
        cargarDatosPerfil();

        // 6. Botón Editar (WIP)
        btnEditarPerfil.setOnClickListener(v -> {
            Toast.makeText(this, "WIP: Abriendo pantalla de edición...", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, EditarPerfilActivity.class);
            // startActivity(intent);
        });
    }

    private void vincularVistas() {
        toolbarPerfil = findViewById(R.id.toolbarPerfil);
        progressBarPerfil = findViewById(R.id.progressBarPerfil);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        textNombreCompleto = findViewById(R.id.textNombreCompleto);
        textCarrera = findViewById(R.id.textCarrera);
        textCorreo = findViewById(R.id.textCorreo);
        textDescripcionPerfil = findViewById(R.id.textDescripcionPerfil);
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        recyclerHabilidades = findViewById(R.id.recyclerHabilidades);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbarPerfil);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Mostrar flecha de regreso
        }
    }

    // Maneja el clic en el botón de "atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y regresa a la anterior (MainActivity)
        return true;
    }

    private void configurarRecyclerHabilidades() {
        habilidadAdapter = new HabilidadAdapter(listaHabilidades);
        // LayoutManager horizontal
        recyclerHabilidades.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHabilidades.setAdapter(habilidadAdapter);
    }

    private void cargarDatosPerfil() {
        progressBarPerfil.setVisibility(View.VISIBLE);

        Call<PerfilEstudiante> call = apiService.getMiPerfil(tokenActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                progressBarPerfil.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Perfil recibido! Pintamos los datos
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(PerfilEstudianteActivity.this, "No se pudo cargar el perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                progressBarPerfil.setVisibility(View.GONE);
                Toast.makeText(PerfilEstudianteActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Llena todas las vistas del layout con la información del objeto PerfilEstudiante.
     */
    private void pintarDatos(PerfilEstudiante perfil) {
        // Asumimos que la API anidó el objeto Usuario dentro de PerfilEstudiante
        if (perfil.getUsuario() != null) {
            String nombre = perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido();
            textNombreCompleto.setText(nombre);
            textCorreo.setText(perfil.getUsuario().getCorreo());
        }

        // Datos del perfil de estudiante
        textCarrera.setText(perfil.getCarrera());
        textDescripcionPerfil.setText(perfil.getDescripcion());

        // (Aquí iría la lógica para cargar la imagen con Glide o Picasso)
        // Glide.with(this).load(perfil.getFotoPerfil()).into(imgFotoPerfil);

        // Llenar la lista de habilidades
        if (perfil.getHabilidades() != null && !perfil.getHabilidades().isEmpty()) {
            listaHabilidades.clear();
            listaHabilidades.addAll(perfil.getHabilidades());
            habilidadAdapter.notifyDataSetChanged(); // Actualizar el RecyclerView
        } else {
            // (Opcional: mostrar un mensaje de "Aún no tienes habilidades")
        }
    }
}