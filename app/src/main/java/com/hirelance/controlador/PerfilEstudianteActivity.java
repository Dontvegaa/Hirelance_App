package com.hirelance.controlador;

// 1. Importa las clases necesarias para la Activity Result API
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent; // <--- AÑADIDO
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Mantenemos la importación de Toolbar
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
    private int idUsuarioActual; // <--- 1. AÑADE ESTA VARIABLE

    // RecyclerView
    private HabilidadAdapter habilidadAdapter;
    private List<Habilidad> listaHabilidades = new ArrayList<>();

    // Datos
    private PerfilEstudiante perfilActual; // <--- AÑADIDO: Variable para guardar el perfil

    // 2. Declara el "Launcher"
    // Este reemplazará al antiguo método 'startActivityForResult'
    private ActivityResultLauncher<Intent> editarPerfilLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_estudiante);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        // 2. OBTÉN EL ID DEL USUARIO
        // (Asumiendo que tu SessionManager tiene un metodo getUserId()
        // basado en donde lo guardaste en LoginActivity)
        idUsuarioActual = sessionManager.getUserId();

        if (tokenActual == null || idUsuarioActual == -1) { // -1 si el ID no se encuentra
            Toast.makeText(this, "Sesión inválida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. REGISTRAR EL LAUNCHER (¡Haz esto en onCreate!)
        // Preparamos cómo vamos a recibir la respuesta
        editarPerfilLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Esto se ejecuta cuando 'EditarPerfilActivity' se cierra
                    if (result.getResultCode() == RESULT_OK) {
                        // ¡Éxito! El usuario guardó cambios.
                        // Volvemos a llamar a la API para refrescar los datos.
                        Toast.makeText(this, "Actualizando perfil...", Toast.LENGTH_SHORT).show();
                        cargarDatosPerfil();
                    }
                    // Si el resultado no es RESULT_OK (ej. el usuario solo presionó "atrás"),
                    // no hacemos nada y los datos no se recargan.
                }
        );

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar RecyclerView de Habilidades
        configurarRecyclerHabilidades();

        // 5. Cargar datos del perfil
        cargarDatosPerfil();

        // 6. Botón Editar (WIP)
        // <--- MODIFICADO: Reemplazamos el Toast por la llamada al nuevo método
        btnEditarPerfil.setOnClickListener(v -> {
            abrirPantallaEdicion();
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

        // Pasa el idUsuarioActual a la llamada de la API
        Call<PerfilEstudiante> call = apiService.getMiPerfil(tokenActual, idUsuarioActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                progressBarPerfil.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // <--- AÑADIDO: Guardamos el perfil en nuestra variable de clase
                    perfilActual = response.body();

                    // ¡Perfil recibido! Pintamos los datos
                    pintarDatos(perfilActual); // Usamos la variable guardada
                } else {
                    Toast.makeText(PerfilEstudianteActivity.this, "No se pudo cargar el perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                progressBarPerfil.setVisibility(View.GONE);
                // ¡IMPORTANTE! Cambia esto para ver el error real si falla
                Toast.makeText(PerfilEstudianteActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
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
            habilidadAdapter.notifyDataSetChanged();
        }

        // <--- AÑADIDO: Hacemos visible el botón solo después de cargar los datos
        btnEditarPerfil.setVisibility(View.VISIBLE);
    }

    // <--- AÑADIDO: Nuevo método para manejar la navegación
    /**
     * Modificado para usar el LAUNCHER en lugar de startActivity.
     */
    private void abrirPantallaEdicion() {
        if (perfilActual == null) {
            Toast.makeText(this, "Aún cargando datos, intente de nuevo.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(PerfilEstudianteActivity.this, EditarPerfilActivity.class);
        intent.putExtra(EditarPerfilActivity.PERFIL_EXTRA, perfilActual);

        // 9. ¡AQUÍ ESTÁ EL CAMBIO!
        // En lugar de: startActivity(intent);
        // Usamos nuestro launcher:
        editarPerfilLauncher.launch(intent);
    }
}