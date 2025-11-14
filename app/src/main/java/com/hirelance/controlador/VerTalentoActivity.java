package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.Habilidad;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.modelo.Universidad;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad dedicada a mostrar el perfil de un estudiante a un Contratista
 * que lo ha encontrado a través de la función "Buscar Talento".
 */
public class VerTalentoActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgFotoPerfil;
    private TextView textNombreCompleto, textCarrera;
    private TextView textDescripcionPerfil;
    private TextView textPortafolioUrl; // ¡NUEVA VISTA!
    private RecyclerView recyclerHabilidades, recyclerUniversidades;

    // Adaptadores
    private HabilidadAdapter habilidadAdapter;
    private UniversidadAdapter universidadAdapter;
    private List<Habilidad> listaHabilidades = new ArrayList<>();
    private List<Universidad> listaUniversidades = new ArrayList<>();

    // Red y Sesión
    private ApiService apiService;
    // private SessionManager sessionManager;
    private String tokenActual;
    private int idUsuarioPerfil = -1;
    private String idUsuarioPerfilStr = null; // Nuevo String para el Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_talento);

        // 1. ¡CAMBIO! Leer datos del Intent
        tokenActual = getIntent().getStringExtra("AUTH_TOKEN");
        idUsuarioPerfilStr = getIntent().getStringExtra("ID_USUARIO_PERFIL");

        // 2. Inicializar ApiService (ya no necesitamos SessionManager aquí)
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 3. ¡CAMBIO! Validar las variables leídas del Intent
        if (tokenActual == null || idUsuarioPerfilStr == null) {
            Log.e("VerTalentoActivity", "Error al iniciar. Token: " + (tokenActual != null) + ", ID Usuario Str: " + idUsuarioPerfilStr);
            Toast.makeText(this, "Error: Datos de sesión o perfil no válidos.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            idUsuarioPerfil = Integer.parseInt(idUsuarioPerfilStr);
        } catch (NumberFormatException e) {
            idUsuarioPerfil = -1; // Fallback
        }

        // 4. Vincular Vistas
        vincularVistas();

        // 5. Configurar Toolbar
        configurarToolbar();

        // 6. Configurar RecyclerViews
        configurarRecyclerHabilidades();
        configurarRecyclerUniversidades();

        // 7. Cargar los datos
        cargarPerfilEstudiante();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarVerTalento); // Nuevo ID
        progressBar = findViewById(R.id.progressBarVerTalento); // Nuevo ID
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        textNombreCompleto = findViewById(R.id.textNombreCompleto);
        textCarrera = findViewById(R.id.textCarrera);
        textDescripcionPerfil = findViewById(R.id.textDescripcionPerfil);
        textPortafolioUrl = findViewById(R.id.textPortafolioUrl); // ¡VINCULACIÓN CLAVE!
        recyclerHabilidades = findViewById(R.id.recyclerHabilidades);
        recyclerUniversidades = findViewById(R.id.recyclerUniversidades);
        // No hay botones ni vistas de postulación
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

    private void configurarRecyclerHabilidades() {
        habilidadAdapter = new HabilidadAdapter(listaHabilidades);
        recyclerHabilidades.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHabilidades.setAdapter(habilidadAdapter);
    }

    private void configurarRecyclerUniversidades() {
        universidadAdapter = new UniversidadAdapter(listaUniversidades, this);
        recyclerUniversidades.setLayoutManager(new LinearLayoutManager(this));
        recyclerUniversidades.setNestedScrollingEnabled(false);
        recyclerUniversidades.setAdapter(universidadAdapter);
    }

    private void cargarPerfilEstudiante() {
        progressBar.setVisibility(View.VISIBLE);

        // ¡CAMBIO! Usar las variables leídas del Intent
        // Asegúrate que ApiService.getPerfilTalento espera un String para el ID
        Call<PerfilEstudiante> call = apiService.getPerfilTalento(tokenActual, idUsuarioPerfilStr);

        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful()) {
                    // ESTO ES LO QUE ESTÁ DANDO 400 (Bad Request)
                    Log.e("VerTalentoActivity", "Error de API: Código " + response.code() + ". Mensaje: " + response.message());
                    Toast.makeText(VerTalentoActivity.this, "Error: Código " + response.code() + ". No se pudo cargar el perfil del estudiante.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.body() != null) {
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(VerTalentoActivity.this, "Respuesta vacía.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("VerTalentoActivity", "Fallo de red", t);
                Toast.makeText(VerTalentoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintarDatos(PerfilEstudiante perfil) {
        if (perfil != null) {
            if (perfil.getUsuario() != null) {
                textNombreCompleto.setText(perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido());
            }
            textCarrera.setText(perfil.getCarrera());
            textDescripcionPerfil.setText(perfil.getDescripcion());

            // --- ¡NUEVA LÓGICA PARA PORTAFOLIO! ---
            if (perfil.getPortafolioUrl() != null && !perfil.getPortafolioUrl().isEmpty()) {
                textPortafolioUrl.setText(perfil.getPortafolioUrl());
                textPortafolioUrl.setVisibility(View.VISIBLE);
                findViewById(R.id.labelPortafolio).setVisibility(View.VISIBLE);
            } else {
                textPortafolioUrl.setVisibility(View.GONE);
                findViewById(R.id.labelPortafolio).setVisibility(View.GONE);
            }

            // Pintar Foto (Base64)
            String fotoBase64 = perfil.getFotoPerfil();
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imgFotoPerfil.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    imgFotoPerfil.setImageResource(R.drawable.ic_menu_perfil);
                }
            } else {
                imgFotoPerfil.setImageResource(R.drawable.ic_menu_perfil);
            }

            // Pintar Habilidades
            if (perfil.getHabilidades() != null && !perfil.getHabilidades().isEmpty()) {
                listaHabilidades.clear();
                listaHabilidades.addAll(perfil.getHabilidades());
                habilidadAdapter.notifyDataSetChanged();
            } else {
                findViewById(R.id.labelHabilidades).setVisibility(View.GONE);
                recyclerHabilidades.setVisibility(View.GONE);
            }

            // Pintar Educación
            if (perfil.getUniversidades() != null && !perfil.getUniversidades().isEmpty()) {
                listaUniversidades.clear();
                listaUniversidades.addAll(perfil.getUniversidades());
                universidadAdapter.notifyDataSetChanged();
            } else {
                findViewById(R.id.labelEducacion).setVisibility(View.GONE);
                recyclerUniversidades.setVisibility(View.GONE);
            }
        }
    }
}