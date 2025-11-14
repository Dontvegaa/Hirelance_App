package com.hirelance.controlador;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.hirelance.R;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.red.RetrofitClient; // Añade este import
import com.hirelance.red.ApiService;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Actividad para que el Contratista busque y explore perfiles de Estudiantes (Talento).
 */
public class BuscarTalentoActivity extends AppCompatActivity {

    private RecyclerView recyclerTalento;
    private TalentoAdapter talentoAdapter;
    private ProgressBar progressBarBuscar;
    private TextView textEmptyStateTalento;
    private TextInputEditText etBuscar;
    private MaterialToolbar toolbarBuscarTalento;

    private List<PerfilEstudiante> listaOriginalEstudiantes = new ArrayList<>();
    private SessionManager sessionManager;
    private ApiService apiService;

    private String tokenHeader; // ¡NUEVO! Variable para guardar el token

    // --- Ciclo de Vida ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buscar_talento);

        // Inicialización
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. ¡CAMBIO! Obtener y guardar el token (incluyendo "Bearer ")
        String token = sessionManager.getToken(); // Asumiendo que getToken() ya funciona
        if (token == null) {
            Toast.makeText(this, "Error crítico de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        this.tokenHeader = "Bearer " + token;

        // Referencias de la vista
        recyclerTalento = findViewById(R.id.recyclerTalento);
        progressBarBuscar = findViewById(R.id.progressBarBuscar);
        textEmptyStateTalento = findViewById(R.id.textEmptyStateTalento);
        etBuscar = findViewById(R.id.etBuscar);
        toolbarBuscarTalento = findViewById(R.id.toolbarBuscarTalento);

        // 4. ¡CAMBIO! Configurar el RecyclerView pasando el token
        talentoAdapter = new TalentoAdapter(new ArrayList<>(), this, this.tokenHeader);
        recyclerTalento.setAdapter(talentoAdapter);

        // Configurar el Toolbar
        setSupportActionBar(toolbarBuscarTalento);
        if (getSupportActionBar() != null) {
            // El icono de navegación (la flecha) ya está definido en el XML
            toolbarBuscarTalento.setNavigationOnClickListener(v -> finish());
        }

        // Configurar el buscador
        setupSearchListener();

        // Cargar los datos
        cargarTalentoEstudiantes();

        // Manejar insets (para compatibilidad con barras del sistema)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

    }

    // --- Métodos de Lógica ---
    private void cargarTalentoEstudiantes() {
        mostrarCarga(true);
        String token = "Bearer " + sessionManager.getToken();

        // ¡CAMBIO! Usar la variable 'tokenHeader' de la clase
        apiService.getTalentoEstudiantes(tokenHeader).enqueue(new Callback<List<PerfilEstudiante>>() {
            @Override
            public void onResponse(Call<List<PerfilEstudiante>> call, Response<List<PerfilEstudiante>> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaOriginalEstudiantes = response.body();
                    filtrarLista(etBuscar.getText().toString()); // Mostrar lista completa o filtrada si ya hay texto
                    checkEmptyState(listaOriginalEstudiantes);
                } else {
                    Toast.makeText(BuscarTalentoActivity.this, "Error al cargar talento: " + response.code(), Toast.LENGTH_LONG).show();
                    checkEmptyState(new ArrayList<>()); // Mostrar Empty State en caso de error
                }
            }

            @Override
            public void onFailure(Call<List<PerfilEstudiante>> call, Throwable t) {
                mostrarCarga(false);
                Log.e("BuscarTalentoActivity", "Fallo de conexión", t);
                Toast.makeText(BuscarTalentoActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                checkEmptyState(new ArrayList<>());
            }
        });
    }

    private void setupSearchListener() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtra la lista en cada cambio de texto
                filtrarLista(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filtrarLista(String textoBusqueda) {
        String query = textoBusqueda.toLowerCase().trim();
        List<PerfilEstudiante> listaFiltrada = new ArrayList<>();

        if (query.isEmpty()) {
            listaFiltrada.addAll(listaOriginalEstudiantes);
        } else {
            for (PerfilEstudiante perfil : listaOriginalEstudiantes) {
                boolean coincide = false;

                // 1. Coincidencia por Nombre/Apellido
                if (perfil.getUsuario() != null) {
                    String nombreCompleto = (perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido()).toLowerCase();
                    if (nombreCompleto.contains(query)) {
                        coincide = true;
                    }
                }

                // 2. Coincidencia por Habilidad Principal (solo la primera habilidad para simplificar)
                if (!coincide && perfil.getHabilidades() != null && !perfil.getHabilidades().isEmpty()) {
                    String habilidad = perfil.getHabilidades().get(0).getTitulo().toLowerCase();
                    if (habilidad.contains(query)) {
                        coincide = true;
                    }
                }

                if (coincide) {
                    listaFiltrada.add(perfil);
                }
            }
        }

        talentoAdapter.setEstudiantes(listaFiltrada);
        checkEmptyState(listaFiltrada);
    }

    private void mostrarCarga(boolean isLoading) {
        progressBarBuscar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerTalento.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        textEmptyStateTalento.setVisibility(View.GONE); // Ocultar siempre el empty state al cargar
    }

    private void checkEmptyState(List<PerfilEstudiante> lista) {
        if (lista.isEmpty() && !progressBarBuscar.isShown()) {
            recyclerTalento.setVisibility(View.GONE);
            textEmptyStateTalento.setVisibility(View.VISIBLE);
        } else if (!progressBarBuscar.isShown()) {
            recyclerTalento.setVisibility(View.VISIBLE);
            textEmptyStateTalento.setVisibility(View.GONE);
        }
    }
}