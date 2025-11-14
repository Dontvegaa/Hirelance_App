package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hirelance.R;
import com.hirelance.modelo.Categoria;
import com.hirelance.modelo.Proyecto;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicarProyectoActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextInputEditText etTitulo, etDescripcion, etPresupuesto, etFechaLimite;
    private AutoCompleteTextView actvCategoria;
    private MaterialButton btnPublicarProyecto;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idContratistaActual;

    // Datos
    // Usamos un HashMap para mapear el NOMBRE de la categoría a su ID
    // Ej: "Desarrollo Web" -> 1
    private HashMap<String, Integer> mapaCategorias = new HashMap<>();
    private List<String> nombresCategorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publicar_proyecto);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();
        idContratistaActual = sessionManager.getUserId();

        if (tokenActual == null) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Vincular y Configurar
        vincularVistas();
        configurarToolbar();

        // 3. Cargar las categorías en el spinner
        cargarCategorias();

        // 4. Configurar botón
        btnPublicarProyecto.setOnClickListener(v -> intentarPublicar());
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarPublicarProyecto);
        progressBar = findViewById(R.id.progressBarPublicar);
        etTitulo = findViewById(R.id.etTituloProyecto);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPresupuesto = findViewById(R.id.etPresupuesto);
        etFechaLimite = findViewById(R.id.etFechaLimite);
        actvCategoria = findViewById(R.id.actvCategoria);
        btnPublicarProyecto = findViewById(R.id.btnPublicarProyecto);
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

    private void cargarCategorias() {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<Categoria>> call = apiService.getCategorias(tokenActual);

        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    // Limpiamos datos anteriores
                    mapaCategorias.clear();
                    nombresCategorias.clear();

                    // Llenamos el mapa y la lista
                    for (Categoria cat : response.body()) {
                        nombresCategorias.add(cat.getNombre());
                        mapaCategorias.put(cat.getNombre(), cat.getIdCategoria());
                    }

                    // Creamos el adaptador y lo asignamos al spinner
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            PublicarProyectoActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            nombresCategorias
                    );
                    actvCategoria.setAdapter(adapter);

                } else {
                    Toast.makeText(PublicarProyectoActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PublicarProyectoActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void intentarPublicar() {
        // 1. Obtener todos los datos
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String presupuestoStr = etPresupuesto.getText().toString().trim();
        String fechaLimite = etFechaLimite.getText().toString().trim();
        String categoriaNombre = actvCategoria.getText().toString();

        // 2. Validar
        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(presupuestoStr) || TextUtils.isEmpty(categoriaNombre)) {
            Toast.makeText(this, "Por favor, llena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double presupuesto;
        try {
            presupuesto = Double.parseDouble(presupuestoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El presupuesto debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Obtener el ID de la categoría desde el HashMap
        Integer idCategoria = mapaCategorias.get(categoriaNombre);
        if (idCategoria == null) {
            Toast.makeText(this, "Categoría no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Crear el objeto Proyecto
        Proyecto nuevoProyecto = new Proyecto();
        nuevoProyecto.setTitulo(titulo);
        nuevoProyecto.setDescripcion(descripcion);
        nuevoProyecto.setPresupuesto(presupuesto);
        nuevoProyecto.setFechaLimite(fechaLimite.isEmpty() ? null : fechaLimite); // Enviar null si está vacío
        nuevoProyecto.setIdContratista(idContratistaActual); // ID del contratista logueado
        nuevoProyecto.setIdCategoria(idCategoria); // ID de la categoría seleccionada

        // 5. Llamar a la API
        progressBar.setVisibility(View.VISIBLE);
        btnPublicarProyecto.setEnabled(false);

        Call<Proyecto> call = apiService.publicarProyecto(tokenActual, nuevoProyecto);
        call.enqueue(new Callback<Proyecto>() {
            @Override
            public void onResponse(Call<Proyecto> call, Response<Proyecto> response) {
                progressBar.setVisibility(View.GONE);
                btnPublicarProyecto.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PublicarProyectoActivity.this, "Proyecto publicado con éxito", Toast.LENGTH_LONG).show();
                    finish(); // Cerrar y volver al dashboard
                } else {
                    Toast.makeText(PublicarProyectoActivity.this, "Error al publicar el proyecto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Proyecto> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnPublicarProyecto.setEnabled(true);
                Toast.makeText(PublicarProyectoActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}