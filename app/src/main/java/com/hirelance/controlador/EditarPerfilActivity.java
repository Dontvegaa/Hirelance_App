package com.hirelance.controlador;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils; // <--- 1. Importa TextUtils
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.content.DialogInterface; // <-- Añadir si falta

import com.hirelance.modelo.Usuario; // Asegúrate de importar Usuario
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hirelance.R;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.red.ApiService; // <--- 2. Importa ApiService
import com.hirelance.red.RetrofitClient; // <--- 3. Importa RetrofitClient
import com.hirelance.util.SessionManager; // <--- 4. Importa SessionManager

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call; // <--- 5. Importa Call, Callback y Response
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    // --- ¡YA NO USAMOS ESTA CONSTANTE! ---
    // public static final String PERFIL_EXTRA = "perfil_estudiante_data";

    // Vistas
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgEditarFotoPreview;
    private MaterialButton btnCambiarFoto, btnGuardarCambios;
    private TextInputEditText etCarrera, etAnioCarrera, etPortafolio, etDescripcion;
    private TextInputEditText etCorreo, etContrasena; // <-- Nuevos campos

    // Datos
    private PerfilEstudiante perfilActual; // Esto se llenará con la API
    private int idUsuarioActual; // Recibiremos esto del Intent

    // Red y Sesión  <--- 6. Añade estas variables
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;

    // --- 3. PARA LA IMAGEN ---
    private ActivityResultLauncher<String> galleryLauncher;
    private String fotoPerfilBase64 = null; // Guardará la *nueva* foto seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        // 7. Inicializar API y Sesión
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

        // 1. Vincular Vistas
        vincularVistas();

        // 2. Configurar Toolbar
        configurarToolbar();

        configurarGalleryLauncher(); // <-- 4. Configura el launcher

        // 4. ¡NUEVO! Cargar datos desde la API
        cargarDatosDelPerfil();

        // 4. Configurar listeners
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnCambiarFoto.setOnClickListener(v -> abrirGaleria()); // <-- 6. Listener para la foto
    }

    // --- 6. ¡NUEVO MÉTODO PARA CARGAR DATOS! ---
    private void cargarDatosDelPerfil() {
        mostrarCarga(true);

        Call<PerfilEstudiante> call = apiService.getMiPerfil(tokenActual, idUsuarioActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    perfilActual = response.body(); // Guardamos el perfil cargado
                    pintarDatos(); // Llamamos a pintarDatos
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

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarEditarPerfil);
        progressBar = findViewById(R.id.progressBarEditarPerfil);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        // Vistas de Foto
        imgEditarFotoPreview = findViewById(R.id.imgEditarFotoPreview);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);

        // Vistas de Perfil
        etCarrera = findViewById(R.id.etCarrera);
        etAnioCarrera = findViewById(R.id.etAnioCarrera);
        etPortafolio = findViewById(R.id.etPortafolio);
        etDescripcion = findViewById(R.id.etDescripcion);

        // Vistas de Cuenta
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        // Añadir flecha de "Atrás"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Listener para la flecha de "Atrás"
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // --- 7. MÉTODO 'recibirDatos()' AHORA SE LLAMA 'pintarDatos()' ---
    // (Ya no recibe un parámetro, usa la variable de clase 'perfilActual')
    private void pintarDatos() {
        if (perfilActual == null) return; // Seguridad

        // Pintar foto actual
        String fotoBase64Actual = perfilActual.getFotoPerfil();
        if (fotoBase64Actual != null && !fotoBase64Actual.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(fotoBase64Actual, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgEditarFotoPreview.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imgEditarFotoPreview.setImageResource(R.drawable.ic_menu_perfil);
            }
        }

        // Pintar campos de perfil
        etCarrera.setText(perfilActual.getCarrera());
        etAnioCarrera.setText(String.valueOf(perfilActual.getAnioCarrera()));
        etPortafolio.setText(perfilActual.getPortafolioUrl());
        etDescripcion.setText(perfilActual.getDescripcion());

        // Pintar campos de cuenta
        if (perfilActual.getUsuario() != null) {
            etCorreo.setText(perfilActual.getUsuario().getCorreo());
        }
    }

    /**
     * Valida los campos y envía la actualización a la API.
     */
    // --- 'guardarCambios()' AHORA TIENE UNA PEQUEÑA MODIFICACIÓN ---
    private void guardarCambios() {
        // ... (Tu validación de campos)
        String carrera = etCarrera.getText().toString().trim();
        String anioStr = etAnioCarrera.getText().toString().trim();
        String portafolio = etPortafolio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // ... (Validación de número de año)
        int anioCarrera = 0;
        try { anioCarrera = Integer.parseInt(anioStr); } catch (NumberFormatException e) { /* ... */ }

        // 10. Actualizar el objeto perfilActual
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

        // --- ¡AÑADE ESTO! ---
        // El script 'actualizarMiPerfil.php' necesita el ID de usuario.
        // Como 'perfilActual' fue cargado por la API, ya debería tenerlo.
        // Pero lo re-seteamos desde el 'idUsuarioActual' para estar 100% seguros.
        perfilActual.setIdUsuario(idUsuarioActual);

        // 11. Llamar a la API
        mostrarCarga(true);

        Call<PerfilEstudiante> call = apiService.actualizarMiPerfil(tokenActual, perfilActual);
        call.enqueue(new Callback<PerfilEstudiante>() {
            @Override
            public void onResponse(Call<PerfilEstudiante> call, Response<PerfilEstudiante> response) {
                mostrarCarga(false);
                // --- ¡CORREGIDO! ---
                // El PHP solo devuelve un mensaje, no el objeto completo
                if (response.isSuccessful()) {
                    Toast.makeText(EditarPerfilActivity.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Avisa a PerfilEstudianteActivity que recargue
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

    /**
     * Muestra u oculta el ProgressBar y habilita/deshabilita el botón.
     */
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
                            imgEditarFotoPreview.setImageBitmap(bitmap); // Poner la preview
                            fotoPerfilBase64 = bitmapToBase64(bitmap); // Guardar el Base64
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