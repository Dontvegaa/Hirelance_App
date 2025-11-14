package com.hirelance.controlador;

// (Android Imports)
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

// (Material Design Imports)
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

// (App Imports)
import com.hirelance.R;
import com.hirelance.modelo.Habilidad;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.modelo.Universidad;
import com.hirelance.modelo.Usuario;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

// (Java Util Imports)
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// (Retrofit Imports)
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity
        implements HabilidadEditableAdapter.OnHabilidadListener,
        UniversidadEditableAdapter.OnUniversidadListener {

    // Vistas
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgEditarFotoPreview;
    private MaterialButton btnCambiarFoto, btnGuardarCambios;
    private TextInputEditText etCarrera, etAnioCarrera, etPortafolio, etDescripcion, etCorreo, etContrasena;
    private RecyclerView recyclerHabilidadesEdit;
    private TextInputEditText etNuevaHabilidad;
    private MaterialButton btnAnadirHabilidad;
    private RecyclerView recyclerUniversidadesEdit;
    private AutoCompleteTextView actvNuevaUniversidad;
    private MaterialButton btnAnadirUniversidad;

    // Adaptadores y Listas
    private HabilidadEditableAdapter habilidadAdapter;
    private List<Habilidad> listaHabilidades = new ArrayList<>();
    private UniversidadEditableAdapter universidadAdapter;
    private List<Universidad> listaUniversidadesEstudiante = new ArrayList<>();
    private HashMap<String, Universidad> mapaUniversidades = new HashMap<>();
    private List<String> nombresUniversidades = new ArrayList<>();

    // Datos
    private PerfilEstudiante perfilActual;
    private int idUsuarioActual;
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private ActivityResultLauncher<String> galleryLauncher;
    private String fotoPerfilBase64 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // 1. Configurar API y Sesión
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getApplicationContext());
        tokenActual = sessionManager.getToken();

        // 2. RECIBIR SOLO EL ID
        idUsuarioActual = getIntent().getIntExtra("ID_USUARIO", -1);

        if (idUsuarioActual == -1 || tokenActual == null) {
            Toast.makeText(this, "Error: Sesión o usuario no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Configurar todo
        vincularVistas();
        configurarToolbar();
        configurarGalleryLauncher();
        configurarRecyclerHabilidades();
        configurarRecyclerUniversidades();

        // 4. Cargar datos
        cargarDatosDelPerfil();
        cargarListaDeUniversidades();

        // 5. Listeners
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnCambiarFoto.setOnClickListener(v -> abrirGaleria());
        btnAnadirHabilidad.setOnClickListener(v -> anadirHabilidad());
        btnAnadirUniversidad.setOnClickListener(v -> anadirUniversidad());
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarEditarPerfil);
        progressBar = findViewById(R.id.progressBarEditarPerfil);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        imgEditarFotoPreview = findViewById(R.id.imgEditarFotoPreview);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        etCarrera = findViewById(R.id.etCarrera);
        etAnioCarrera = findViewById(R.id.etAnioCarrera);
        etPortafolio = findViewById(R.id.etPortafolio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);

        // Habilidades
        recyclerHabilidadesEdit = findViewById(R.id.recyclerHabilidadesEdit);
        etNuevaHabilidad = findViewById(R.id.etNuevaHabilidad);
        btnAnadirHabilidad = findViewById(R.id.btnAnadirHabilidad);

        // --- ¡VÍNCULOS CORREGIDOS! ---
        recyclerUniversidadesEdit = findViewById(R.id.recyclerUniversidadesEdit);
        actvNuevaUniversidad = findViewById(R.id.actvNuevaUniversidad);
        btnAnadirUniversidad = findViewById(R.id.btnAnadirUniversidad);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarRecyclerHabilidades() {
        habilidadAdapter = new HabilidadEditableAdapter(listaHabilidades, this, this);
        recyclerHabilidadesEdit.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHabilidadesEdit.setAdapter(habilidadAdapter);
    }

    private void configurarRecyclerUniversidades() {
        universidadAdapter = new UniversidadEditableAdapter(listaUniversidadesEstudiante, this, this);
        recyclerUniversidadesEdit.setLayoutManager(new LinearLayoutManager(this));
        recyclerUniversidadesEdit.setNestedScrollingEnabled(false);
        recyclerUniversidadesEdit.setAdapter(universidadAdapter);
    }

    private void cargarDatosDelPerfil() {
        mostrarCarga(true);
        Call<PerfilEstudiante> call = apiService.getMiPerfil(tokenActual, idUsuarioActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    perfilActual = response.body();
                    pintarDatos();
                } else {
                    Toast.makeText(EditarPerfilActivity.this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                mostrarCarga(false);
                mostrarErrorDialog("Error (onFailure)", t.getMessage());
                finish();
            }
        });
    }

    private void cargarListaDeUniversidades() {
        Call<List<Universidad>> call = apiService.getUniversidades(tokenActual);
        call.enqueue(new Callback<List<Universidad>>() {
            @Override
            public void onResponse(Call<List<Universidad>> call, Response<List<Universidad>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mapaUniversidades.clear();
                    nombresUniversidades.clear();
                    for (Universidad uni : response.body()) {
                        nombresUniversidades.add(uni.getNombre());
                        mapaUniversidades.put(uni.getNombre(), uni);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            EditarPerfilActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            nombresUniversidades
                    );
                    actvNuevaUniversidad.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Universidad>> call, Throwable t) {
                Toast.makeText(EditarPerfilActivity.this, "Error al cargar lista de universidades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pintarDatos() {
        if (perfilActual == null) return;

        String fotoBase64Actual = perfilActual.getFotoPerfil();
        if (fotoBase64Actual != null && !fotoBase64Actual.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fotoBase64Actual, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgEditarFotoPreview.setImageBitmap(decodedByte);
            } catch (Exception e) { imgEditarFotoPreview.setImageResource(R.drawable.ic_menu_perfil); }
        }

        etCarrera.setText(perfilActual.getCarrera());
        etAnioCarrera.setText(String.valueOf(perfilActual.getAnioCarrera()));
        etPortafolio.setText(perfilActual.getPortafolioUrl());
        etDescripcion.setText(perfilActual.getDescripcion());

        if (perfilActual.getUsuario() != null) {
            etCorreo.setText(perfilActual.getUsuario().getCorreo());
        }

        if (perfilActual.getHabilidades() != null) {
            listaHabilidades.clear();
            listaHabilidades.addAll(perfilActual.getHabilidades());
            habilidadAdapter.notifyDataSetChanged();
        }

        if (perfilActual.getUniversidades() != null) {
            listaUniversidadesEstudiante.clear();
            listaUniversidadesEstudiante.addAll(perfilActual.getUniversidades());
            universidadAdapter.notifyDataSetChanged();
        }
    }

    private void anadirHabilidad() {
        String tituloHabilidad = etNuevaHabilidad.getText().toString().trim();
        if (tituloHabilidad.isEmpty()) {
            Toast.makeText(this, "Escribe una habilidad", Toast.LENGTH_SHORT).show();
            return;
        }
        Habilidad nuevaHabilidad = new Habilidad();
        nuevaHabilidad.setTitulo(tituloHabilidad);
        listaHabilidades.add(nuevaHabilidad);
        habilidadAdapter.notifyItemInserted(listaHabilidades.size() - 1);
        etNuevaHabilidad.setText("");
    }

    @Override
    public void onHabilidadEliminarClick(int position) {
        listaHabilidades.remove(position);
        habilidadAdapter.notifyItemRemoved(position);
        habilidadAdapter.notifyItemRangeChanged(position, listaHabilidades.size());
    }

    private void anadirUniversidad() {
        String nombreUni = actvNuevaUniversidad.getText().toString();
        if (nombreUni.isEmpty() || !mapaUniversidades.containsKey(nombreUni)) {
            Toast.makeText(this, "Selecciona una universidad válida de la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        Universidad uniSeleccionada = mapaUniversidades.get(nombreUni);

        for(Universidad uni : listaUniversidadesEstudiante) {
            if(uni.getIdUniversidad() == uniSeleccionada.getIdUniversidad()) {
                Toast.makeText(this, "Esa universidad ya está en tu lista", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        listaUniversidadesEstudiante.add(uniSeleccionada);
        universidadAdapter.notifyItemInserted(listaUniversidadesEstudiante.size() - 1);
        actvNuevaUniversidad.setText("", false);
    }

    @Override
    public void onUniversidadEliminarClick(int position) {
        listaUniversidadesEstudiante.remove(position);
        universidadAdapter.notifyItemRemoved(position);
        universidadAdapter.notifyItemRangeChanged(position, listaUniversidadesEstudiante.size());
    }

    private void guardarCambios() {
        String carrera = etCarrera.getText().toString().trim();
        String anioStr = etAnioCarrera.getText().toString().trim();
        String portafolio = etPortafolio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        int anioCarrera = 0;
        try { anioCarrera = Integer.parseInt(anioStr); } catch (NumberFormatException e) { /* ... */ }

        perfilActual.setCarrera(carrera);
        perfilActual.setAnioCarrera(anioCarrera);
        perfilActual.setPortafolioUrl(portafolio);
        perfilActual.setDescripcion(descripcion);
        if (fotoPerfilBase64 != null) {
            perfilActual.setFotoPerfil(fotoPerfilBase64);
        }
        if (perfilActual.getUsuario() == null) {
            perfilActual.setUsuario(new Usuario());
        }
        perfilActual.getUsuario().setCorreo(correo);
        if (!contrasena.isEmpty()) {
            perfilActual.getUsuario().setContrasena(contrasena);
        } else {
            perfilActual.getUsuario().setContrasena(null);
        }
        perfilActual.setIdUsuario(idUsuarioActual);
        perfilActual.setHabilidades(this.listaHabilidades);
        perfilActual.setUniversidades(this.listaUniversidadesEstudiante);

        mostrarCarga(true);
        Call<PerfilEstudiante> call = apiService.actualizarMiPerfil(tokenActual, perfilActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                mostrarCarga(false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditarPerfilActivity.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMsg = "Error al actualizar. Código: " + response.code();
                    if (response.errorBody() != null) {
                        try { errorMsg = response.errorBody().string(); } catch (IOException e) { e.printStackTrace(); }
                    }
                    mostrarErrorDialog("Error del Servidor", errorMsg);
                }
            }
            @Override
            public void onFailure(Call<PerfilEstudiante> call, Throwable t) {
                mostrarCarga(false);
                mostrarErrorDialog("Error (onFailure)", t.getMessage());
            }
        });
    }

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardarCambios.setEnabled(!cargando);
    }

    private void configurarGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = uriToBitmap(uri);
                            imgEditarFotoPreview.setImageBitmap(bitmap);
                            fotoPerfilBase64 = bitmapToBase64(bitmap);
                            Log.d("EditarPerfil", "Nueva foto seleccionada y convertida a Base64.");
                        } catch (IOException e) {
                            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void abrirGaleria() { galleryLauncher.launch("image/*"); }

    private Bitmap uriToBitmap(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source);
        } else {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void mostrarErrorDialog(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_menu_perfil)
                .show();
    }
}