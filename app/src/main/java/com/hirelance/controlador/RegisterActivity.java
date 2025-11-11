package com.hirelance.controlador;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog; // <--- 1. IMPORTA ESTO
import android.content.DialogInterface; // <--- 2. IMPORTA ESTO

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hirelance.R;
import com.hirelance.modelo.RegisterEstudianteDTO; // <--- 1. IMPORTA EL DTO
import com.hirelance.modelo.Usuario;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Vistas
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgFotoPerfilPreview;
    private MaterialButton btnSeleccionarFoto, btnRegistrar;

    // --- Campos de Usuario ---
    private TextInputLayout tilNombre, tilApellido, tilCorreo, tilContrasena, tilDUI, tilTelefono;
    private TextInputEditText etNombre, etApellido, etCorreo, etContrasena, etDUI, etTelefono;

    // --- Campos de Perfil Estudiante ---
    private TextInputLayout tilCarrera, tilPortafolio, tilHabilidades, tilDescripcion, tilAnioCarrera;
    private TextInputEditText etCarrera, etPortafolio, etHabilidades, etDescripcion;
    private AutoCompleteTextView actvAnioCarrera;

    // Red
    private ApiService apiService;

    // --- Para la Imagen ---
    private ActivityResultLauncher<String> galleryLauncher;
    private String fotoPerfilBase64 = null; // Aquí guardaremos el String Base64

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inicializar API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. Vincular todas las vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Configurar el Spinner (AutoCompleteTextView)
        configurarSpinnerAnio();

        // 5. Configurar el Lanzador de la Galería (para la foto)
        configurarGalleryLauncher();

        // 6. Configurar Listeners de los botones
        btnSeleccionarFoto.setOnClickListener(v -> abrirGaleria());
        btnRegistrar.setOnClickListener(v -> intentarRegistro());
    }

    // ... (vincularVistas, configurarToolbar, configurarSpinnerAnio, configurarGalleryLauncher, abrirGaleria...
    // ... todos estos métodos son idénticos al paso anterior) ...

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarRegister);
        progressBar = findViewById(R.id.progressBarRegister);
        imgFotoPerfilPreview = findViewById(R.id.imgFotoPerfilPreview);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnRegistrar = findViewById(R.id.btnRegistrar);
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
        tilCarrera = findViewById(R.id.tilCarrera);
        etCarrera = findViewById(R.id.etCarrera);
        tilAnioCarrera = findViewById(R.id.tilAnioCarrera);
        actvAnioCarrera = findViewById(R.id.actvAnioCarrera);
        tilPortafolio = findViewById(R.id.tilPortafolio);
        etPortafolio = findViewById(R.id.etPortafolio);
        tilHabilidades = findViewById(R.id.tilHabilidades);
        etHabilidades = findViewById(R.id.etHabilidades);
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

    private void configurarSpinnerAnio() {
        String[] anios = getResources().getStringArray(R.array.anios_carrera);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, anios);
        actvAnioCarrera.setAdapter(adapter);
    }

    private void configurarGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = uriToBitmap(uri);
                            imgFotoPerfilPreview.setImageBitmap(bitmap);
                            fotoPerfilBase64 = bitmapToBase64(bitmap);
                            Log.d("RegisterActivity", "Imagen convertida a Base64.");
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


    /**
     * Recolecta todos los datos del formulario, los valida,
     * crea el DTO y llama a la API.
     */
    private void intentarRegistro() {
        // --- 1. VALIDACIÓN (Simplificada, puedes añadir más) ---
        if (!validarFormulario()) {
            Toast.makeText(this, "Por favor, corrige los campos marcados", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- 2. RECOLECCIÓN DE DATOS ---
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();
        String dui = etDUI.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        String carrera = etCarrera.getText().toString().trim();
        String anioCarreraStr = actvAnioCarrera.getText().toString();
        int anioCarrera = 1; // Default
        String[] aniosArray = getResources().getStringArray(R.array.anios_carrera);
        for(int i = 0; i < aniosArray.length; i++) {
            if(aniosArray[i].equals(anioCarreraStr)) {
                anioCarrera = i + 1;
                break;
            }
        }

        String portafolio = etPortafolio.getText().toString().trim();
        String habilidades = etHabilidades.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        // La variable 'fotoPerfilBase64' ya tiene la imagen (o es null)

        // --- 3. CREAR EL DTO (¡CORREGIDO!) ---
        RegisterEstudianteDTO dto = new RegisterEstudianteDTO();
        // Datos de Usuario
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setCorreo(correo);
        dto.setContrasena(contrasena);
        dto.setDui(dui);
        dto.setTelefono(telefono);
        // Datos de Perfil
        dto.setCarrera(carrera);
        dto.setAnio_carrera(anioCarrera); // <-- CORREGIDO
        dto.setPortafolio_url(portafolio.isEmpty() ? null : portafolio); // <-- CORREGIDO
        dto.setHabilidades(habilidades.isEmpty() ? null : habilidades);
        dto.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        dto.setFoto_perfil(fotoPerfilBase64); // <-- CORREGIDO

        // --- 4. LLAMAR A LA API ---
        mostrarCarga(true);

        Call<Usuario> call = apiService.registerEstudiante(dto);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                mostrarCarga(false);
                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito!
                    Toast.makeText(RegisterActivity.this, "¡Registro exitoso! Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
                    finish(); // Regresa a LoginActivity
                } else {
                    // --- 3. ¡BLOQUE DE ERROR onResponse ACTUALIZADO! ---
                    String errorMsg = "Error desconocido.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    // Ya no usamos Toast, usamos el diálogo
                    mostrarErrorDialog("Error del Servidor (Cód: " + response.code() + ")", errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                mostrarCarga(false);
                // --- 4. ¡BLOQUE DE ERROR onFailure ACTUALIZADO! ---
                String errorCompleto = t.getMessage();
                Log.e("REGISTER_ERROR", "onFailure: " + errorCompleto, t);

                // Ya no usamos Toast, usamos el diálogo
                mostrarErrorDialog("Error en onFailure (GSON/Red)", errorCompleto);
            }
        });
    }

    /**
     * Valida los campos principales del formulario.
     * (Puedes expandir esto)
     */
    private boolean validarFormulario() {
        boolean esValido = true;

        // Limpiar errores previos
        tilNombre.setError(null);
        tilApellido.setError(null);
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        tilDUI.setError(null);
        tilCarrera.setError(null);

        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) {
            tilNombre.setError("Campo requerido"); esValido = false;
        }
        if (TextUtils.isEmpty(etApellido.getText().toString().trim())) {
            tilApellido.setError("Campo requerido"); esValido = false;
        }
        if (TextUtils.isEmpty(etDUI.getText().toString().trim())) {
            tilDUI.setError("Campo requerido"); esValido = false;
        }
        if (TextUtils.isEmpty(etCarrera.getText().toString().trim())) {
            tilCarrera.setError("Campo requerido"); esValido = false;
        }
        if (TextUtils.isEmpty(etCorreo.getText().toString().trim())) {
            tilCorreo.setError("Campo requerido"); esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString().trim()).matches()) {
            tilCorreo.setError("Correo inválido"); esValido = false;
        }
        if (TextUtils.isEmpty(etContrasena.getText().toString().trim())) {
            tilContrasena.setError("Campo requerido"); esValido = false;
        }

        return esValido;
    }

    private void mostrarCarga(boolean cargando) {
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!cargando);
    }

    // --- Métodos de Ayuda para la Imagen (Sin cambios) ---

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

    // --- 5. ¡NUEVO METODO DE DIÁLOGO! ---
    /**
     * Muestra un AlertDialog con el título y el mensaje de error.
     */
    private void mostrarErrorDialog(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje) // Ahora mostrará el mensaje completo
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.ic_menu_perfil) // Puedes cambiar este ícono
                .show();
    }
}