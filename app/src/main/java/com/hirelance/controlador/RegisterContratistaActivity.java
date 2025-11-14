package com.hirelance.controlador;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import com.google.android.material.textfield.TextInputLayout;
import com.hirelance.R;
import com.hirelance.modelo.RegisterContratistaDTO;
import com.hirelance.modelo.Usuario;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterContratistaActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgLogoPreview;
    private MaterialButton btnSeleccionarLogo, btnRegistrarContratista;

    // Campos de Usuario
    private TextInputLayout tilNombre, tilApellido, tilCorreo, tilContrasena, tilDUI, tilTelefono;
    private TextInputEditText etNombre, etApellido, etCorreo, etContrasena, etDUI, etTelefono;

    // Campos de Perfil Contratista
    private TextInputLayout tilNombreEmpresa, tilUbicacion, tilSitioWeb, tilDescripcion;
    private TextInputEditText etNombreEmpresa, etUbicacion, etSitioWeb, etDescripcion;

    // Red
    private ApiService apiService;

    // Para la Imagen
    private ActivityResultLauncher<String> galleryLauncher;
    private String logoPerfilBase64 = null; // String Base64 para el MEDIUMBLOB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_contratista);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        vincularVistas();
        configurarToolbar();
        configurarGalleryLauncher();

        btnSeleccionarLogo.setOnClickListener(v -> abrirGaleria());
        btnRegistrarContratista.setOnClickListener(v -> intentarRegistro());
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarRegisterContratista);
        progressBar = findViewById(R.id.progressBarRegister);
        imgLogoPreview = findViewById(R.id.imgLogoPreview);
        btnSeleccionarLogo = findViewById(R.id.btnSeleccionarLogo);
        btnRegistrarContratista = findViewById(R.id.btnRegistrarContratista);

        // Campos de Usuario
        tilNombre = findViewById(R.id.tilNombre);
        etNombre = findViewById(R.id.etNombre);
        tilApellido = findViewById(R.id.tilApellido);
        etApellido = findViewById(R.id.etApellido);
        tilCorreo = findViewById(R.id.tilCorreo);
        etCorreo = findViewById(R.id.etCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        etContrasena = findViewById(R.id.etContrasena);
        tilDUI = findViewById(R.id.tilDUI);
        etDUI = findViewById(R.id.etDUI);
        tilTelefono = findViewById(R.id.tilTelefono);
        etTelefono = findViewById(R.id.etTelefono);

        // Campos de Perfil Contratista
        tilNombreEmpresa = findViewById(R.id.tilNombreEmpresa);
        etNombreEmpresa = findViewById(R.id.etNombreEmpresa);
        tilUbicacion = findViewById(R.id.tilUbicacion);
        etUbicacion = findViewById(R.id.etUbicacion);
        tilSitioWeb = findViewById(R.id.tilSitioWeb);
        etSitioWeb = findViewById(R.id.etSitioWeb);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        etDescripcion = findViewById(R.id.etDescripcion);
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configurarGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = uriToBitmap(uri);
                            imgLogoPreview.setImageBitmap(bitmap);
                            logoPerfilBase64 = bitmapToBase64(bitmap);
                        } catch (IOException e) {
                            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void abrirGaleria() {
        galleryLauncher.launch("image/*");
    }

    private void intentarRegistro() {
        if (!validarFormulario()) {
            Toast.makeText(this, "Por favor, corrige los campos marcados", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Recolectar datos
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String dui = etDUI.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        String nombreEmpresa = etNombreEmpresa.getText().toString().trim();
        String ubicacion = etUbicacion.getText().toString().trim();
        String sitioWeb = etSitioWeb.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        // 'logoPerfilBase64' ya tiene la imagen (o es null)

        // 3. Crear el DTO
        RegisterContratistaDTO dto = new RegisterContratistaDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setCorreo(correo);
        dto.setContrasena(contrasena);
        dto.setDui(dui);
        dto.setTelefono(telefono);
        dto.setEmpresa(nombreEmpresa);
        dto.setUbicacion(ubicacion);
        dto.setSitioWeb(sitioWeb.isEmpty() ? null : sitioWeb);
        dto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        dto.setLogoBase64(logoPerfilBase64);

        // 4. Llamar a la API
        mostrarCarga(true);
        Call<Usuario> call = apiService.registerContratista(dto);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterContratistaActivity.this, "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
                    finish(); // Regresa a la pantalla anterior (probablemente Login o selector)
                } else {
                    String errorMsg = "Error en el registro.";
                    if (response.errorBody() != null) {
                        try { errorMsg = response.errorBody().string(); } catch (IOException e) { e.printStackTrace(); }
                    }
                    mostrarErrorDialog("Error del Servidor (Cód: " + response.code() + ")", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                mostrarCarga(false);
                mostrarErrorDialog("Error en onFailure (GSON/Red)", t.getMessage());
            }
        });
    }

    private boolean validarFormulario() {
        // Validación básica (puedes expandirla)
        boolean esValido = true;

        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) esValido = false;
        if (TextUtils.isEmpty(etApellido.getText().toString().trim())) esValido = false;
        if (TextUtils.isEmpty(etDUI.getText().toString().trim())) esValido = false;
        if (TextUtils.isEmpty(etTelefono.getText().toString().trim())) esValido = false;
        if (TextUtils.isEmpty(etNombreEmpresa.getText().toString().trim())) esValido = false;
        if (TextUtils.isEmpty(etCorreo.getText().toString().trim()) || !Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString().trim()).matches()) esValido = false;
        if (TextUtils.isEmpty(etContrasena.getText().toString().trim())) esValido = false;

        // (Añade 'setError' a los TextInputLayouts si quieres)

        return esValido;
    }

    // --- Métodos de Ayuda (Carga, Imagen, Diálogo) ---

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistrarContratista.setEnabled(!cargando);
    }

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