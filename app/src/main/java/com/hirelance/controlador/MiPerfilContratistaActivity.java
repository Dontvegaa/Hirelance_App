package com.hirelance.controlador;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.hirelance.R;
import com.hirelance.modelo.PerfilContratista;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiPerfilContratistaActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgLogoEmpresa;
    private TextView textNombreEmpresa, textNombreContratista, textCorreoContratista;
    private TextView textDescripcionEmpresa, textSitioWeb, textTelefono, textDireccion;
    private MaterialButton btnEditarPerfilContratista;

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idUsuarioActual;

    // --- 1. AÑADE EL LAUNCHER ---
    private ActivityResultLauncher<Intent> editarPerfilLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil_contratista);

        // --- 2. REGISTRA EL LAUNCHER ---
        editarPerfilLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Esto se ejecuta cuando 'EditarPerfilContratistaActivity' se cierra
                    if (result.getResultCode() == RESULT_OK) {
                        // ¡Éxito! El usuario guardó cambios.
                        // Volvemos a llamar a la API para refrescar los datos.
                        Toast.makeText(this, "Actualizando perfil...", Toast.LENGTH_SHORT).show();
                        cargarDatosPerfil();
                    }
                }
        );

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();
        idUsuarioActual = sessionManager.getUserId();

        if (tokenActual == null || idUsuarioActual == -1) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Toolbar
        configurarToolbar();

        // 4. Cargar Datos
        cargarDatosPerfil();

        // --- 3. ACTUALIZA EL LISTENER ---
        btnEditarPerfilContratista.setOnClickListener(v -> {
            // Abrimos la pantalla de edición
            Intent intent = new Intent(this, EditarPerfilContratistaActivity.class);
            // Le pasamos solo el ID (para evitar TransactionTooLargeException)
            intent.putExtra("ID_USUARIO", idUsuarioActual);
            editarPerfilLauncher.launch(intent); // Usamos el launcher
        });
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarMiPerfilContratista);
        progressBar = findViewById(R.id.progressBarPerfilContratista);
        imgLogoEmpresa = findViewById(R.id.imgLogoEmpresa);
        textNombreEmpresa = findViewById(R.id.textNombreEmpresa);
        textNombreContratista = findViewById(R.id.textNombreContratista);
        textCorreoContratista = findViewById(R.id.textCorreoContratista);
        textDescripcionEmpresa = findViewById(R.id.textDescripcionEmpresa);
        textSitioWeb = findViewById(R.id.textSitioWeb);
        textTelefono = findViewById(R.id.textTelefono);
        textDireccion = findViewById(R.id.textDireccion);
        btnEditarPerfilContratista = findViewById(R.id.btnEditarPerfilContratista);
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

    private void cargarDatosPerfil() {
        progressBar.setVisibility(View.VISIBLE);
        Call<PerfilContratista> call = apiService.getMiPerfilContratista(tokenActual, idUsuarioActual);

        call.enqueue(new Callback<PerfilContratista>() {
            @Override
            public void onResponse(Call<PerfilContratista> call, Response<PerfilContratista> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(MiPerfilContratistaActivity.this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PerfilContratista> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MiPerfilContratistaActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintarDatos(PerfilContratista perfil) {
        // Pintar Logo (Base64)
        String logoBase64 = perfil.getLogoBase64();
        if (logoBase64 != null && !logoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(logoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgLogoEmpresa.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imgLogoEmpresa.setImageResource(R.drawable.ic_menu_perfil); // Fallback
            }
        }

        // Pintar datos del perfil
        textNombreEmpresa.setText(perfil.getNombreEmpresa());
        textDescripcionEmpresa.setText(perfil.getDescripcion());
        textDireccion.setText(perfil.getDireccion());

        // Pintar datos del usuario anidado
        if (perfil.getUsuario() != null) {
            String nombreCompleto = perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido();
            textNombreContratista.setText(nombreCompleto);
            textCorreoContratista.setText(perfil.getUsuario().getCorreo());
            textTelefono.setText(perfil.getUsuario().getTelefono());
        }

        // Pintar Sitio Web (y hacerlo clicable)
        if (perfil.getSitioWeb() != null && !perfil.getSitioWeb().isEmpty()) {
            textSitioWeb.setText(perfil.getSitioWeb());
            android.text.util.Linkify.addLinks(textSitioWeb, android.text.util.Linkify.WEB_URLS);
        } else {
            textSitioWeb.setText("No especificado");
        }
    }
}