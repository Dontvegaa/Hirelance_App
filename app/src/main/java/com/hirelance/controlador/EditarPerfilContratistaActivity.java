package com.hirelance.controlador;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hirelance.R;
import com.hirelance.modelo.PerfilContratista;
import com.hirelance.modelo.Usuario;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilContratistaActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgEditarLogoPreview;
    private MaterialButton btnCambiarLogo, btnGuardarCambios;
    private TextInputEditText etNombreEmpresa, etDescripcionEmpresa, etSitioWeb, etDireccion;
    private TextInputEditText etCorreo, etTelefono, etContrasena;

    // Datos
    private PerfilContratista perfilActual;
    private int idUsuarioActual;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;

    // Para la Imagen
    private ActivityResultLauncher<String> galleryLauncher;
    private String logoPerfilBase64 = null; // Guardará el *nuevo* logo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_contratista);

        // 1. Configurar API y Sesión
        apiService = RetrofitClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(getApplicationContext());
        tokenActual = sessionManager.getToken();

        // 2. Recibir ID de la activity anterior (MiPerfilContratistaActivity)
        idUsuarioActual = getIntent().getIntExtra("ID_USUARIO", -1);

        if (idUsuarioActual == -1 || tokenActual == null) {
            Toast.makeText(this, "Error: Sesión o usuario no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Configurar
        vincularVistas();
        configurarToolbar();
        configurarGalleryLauncher();

        // 4. Cargar datos
        cargarDatosDelPerfil();

        // 5. Listeners
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
        btnCambiarLogo.setOnClickListener(v -> abrirGaleria());
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarEditarPerfilContratista);
        progressBar = findViewById(R.id.progressBarEditarContratista);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambiosContratista);
        imgEditarLogoPreview = findViewById(R.id.imgEditarLogoPreview);
        btnCambiarLogo = findViewById(R.id.btnCambiarLogo);

        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        etDescripcionEmpresa = findViewById(R.id.etDescripcionEmpresa);
        etSitioWeb = findViewById(R.id.etSitioWeb);
        etDireccion = findViewById(R.id.etDireccion);

        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etContrasena = findViewById(R.id.etContrasena);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void cargarDatosDelPerfil() {
        mostrarCarga(true);
        Call<PerfilContratista> call = apiService.getMiPerfilContratista(tokenActual, idUsuarioActual);
        call.enqueue(new Callback<PerfilContratista>() {
            @Override
            public void onResponse(Call<PerfilContratista> call, Response<PerfilContratista> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    perfilActual = response.body();
                    pintarDatos();
                } else {
                    Toast.makeText(EditarPerfilContratistaActivity.this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<PerfilContratista> call, Throwable t) {
                mostrarCarga(false);
                mostrarErrorDialog("Error (onFailure)", t.getMessage());
                finish();
            }
        });
    }

    private void pintarDatos() {
        if (perfilActual == null) return;

        // Pintar logo actual
        String logoBase64Actual = perfilActual.getLogoBase64();
        if (logoBase64Actual != null && !logoBase64Actual.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(logoBase64Actual, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgEditarLogoPreview.setImageBitmap(decodedByte);
            } catch (Exception e) { /* fallback */ }
        }

        // Pintar datos de empresa
        etNombreEmpresa.setText(perfilActual.getNombreEmpresa());
        etDescripcionEmpresa.setText(perfilActual.getDescripcion());
        etSitioWeb.setText(perfilActual.getSitioWeb());
        etDireccion.setText(perfilActual.getDireccion());

        // Pintar datos de contacto (del usuario anidado)
        if (perfilActual.getUsuario() != null) {
            etCorreo.setText(perfilActual.getUsuario().getCorreo());
            etTelefono.setText(perfilActual.getUsuario().getTelefono());
        }
    }

    private void guardarCambios() {
        // 1. Recolectar datos del formulario
        String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        String descripcion = etDescripcionEmpresa.getText().toString().trim();
        String sitioWeb = etSitioWeb.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();

        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        // 2. Validar (simplificado)
        if (TextUtils.isEmpty(nombreEmpresa) || TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Nombre de empresa y correo son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Actualizar el objeto 'perfilActual'
        perfilActual.setNombreEmpresa(nombreEmpresa);
        perfilActual.setDescripcion(descripcion);
        perfilActual.setSitioWeb(sitioWeb);
        perfilActual.setDireccion(direccion);

        // Si se seleccionó un nuevo logo, lo añadimos
        if (logoPerfilBase64 != null) {
            perfilActual.setLogoBase64(logoPerfilBase64);
        }

        // Actualizar el objeto 'usuario' anidado
        if (perfilActual.getUsuario() == null) {
            perfilActual.setUsuario(new Usuario());
        }
        perfilActual.getUsuario().setCorreo(correo);
        perfilActual.getUsuario().setTelefono(telefono);
        perfilActual.setIdUsuario(idUsuarioActual); // Asegurar que el ID de usuario esté

        if (!contrasena.isEmpty()) {
            perfilActual.getUsuario().setContrasena(contrasena);
        } else {
            perfilActual.getUsuario().setContrasena(null); // No cambiar si está vacío
        }

        // 4. Llamar a la API
        mostrarCarga(true);
        Call<PerfilContratista> call = apiService.actualizarMiPerfilContratista(tokenActual, perfilActual);

        call.enqueue(new Callback<PerfilContratista>() {
            @Override
            public void onResponse(Call<PerfilContratista> call, Response<PerfilContratista> response) {
                mostrarCarga(false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditarPerfilContratistaActivity.this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Avisar a la pantalla anterior para que recargue
                    finish();
                } else {
                    String errorMsg = "Error al actualizar.";
                    try { errorMsg = response.errorBody().string(); } catch (Exception e) {}
                    mostrarErrorDialog("Error del Servidor", errorMsg);
                }
            }
            @Override
            public void onFailure(Call<PerfilContratista> call, Throwable t) {
                mostrarCarga(false);
                mostrarErrorDialog("Error (onFailure)", t.getMessage());
            }
        });
    }

    // --- Lógica de Galería (Idéntica a EditarPerfilActivity) ---

    private void configurarGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = uriToBitmap(uri);
                            imgEditarLogoPreview.setImageBitmap(bitmap);
                            logoPerfilBase64 = bitmapToBase64(bitmap); // Guardar el Base64
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

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardarCambios.setEnabled(!cargando);
    }
}