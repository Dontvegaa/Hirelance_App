package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hirelance.R;
import com.hirelance.modelo.DetallePostulante;
import com.hirelance.modelo.Habilidad;
import com.hirelance.modelo.PerfilEstudiante;
import com.hirelance.modelo.Postulacion;
import com.hirelance.modelo.Universidad;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;
import com.hirelance.util.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePostulanteActivity extends AppCompatActivity {

    // Vistas
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageView imgFotoPerfil;
    private TextView textNombreCompleto, textCarrera, textPropuesta, textOferta, textTiempo;
    private TextView textDescripcionPerfil;
    private RecyclerView recyclerHabilidades, recyclerUniversidades;
    private Button btnRechazar, btnAceptar;

    // Adaptadores (Reutilizamos los que ya creamos)
    private HabilidadAdapter habilidadAdapter;
    private UniversidadAdapter universidadAdapter;
    private List<Habilidad> listaHabilidades = new ArrayList<>();
    private List<Universidad> listaUniversidades = new ArrayList<>();

    // Red y Sesión
    private ApiService apiService;
    private SessionManager sessionManager;
    private String tokenActual;
    private int idPostulacionActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_postulante);

        // 1. Configurar Sesión y API
        sessionManager = new SessionManager(getApplicationContext());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        tokenActual = sessionManager.getToken();

        // 2. Obtener el ID de la postulación (enviado por PostulanteAdapter)
        idPostulacionActual = getIntent().getIntExtra("ID_POSTULACION", -1);

        if (tokenActual == null || idPostulacionActual == -1) {
            Toast.makeText(this, "Error: Postulación o sesión no válidas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Vincular Vistas
        vincularVistas();

        // 4. Configurar Toolbar
        configurarToolbar();

        // 5. Configurar RecyclerViews
        configurarRecyclerHabilidades();
        configurarRecyclerUniversidades();

        // 6. Configurar Listeners de botones
        btnAceptar.setOnClickListener(v -> aceptarPostulacion());
        btnRechazar.setOnClickListener(v -> rechazarPostulacion());

        // 7. Cargar todos los datos
        cargarDetallePostulante();
    }

    private void vincularVistas() {
        toolbar = findViewById(R.id.toolbarDetallePostulante);
        progressBar = findViewById(R.id.progressBarDetallePostulante);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        textNombreCompleto = findViewById(R.id.textNombreCompleto);
        textCarrera = findViewById(R.id.textCarrera);
        textPropuesta = findViewById(R.id.textPropuesta);
        textOferta = findViewById(R.id.textOferta);
        textTiempo = findViewById(R.id.textTiempo);
        textDescripcionPerfil = findViewById(R.id.textDescripcionPerfil);
        recyclerHabilidades = findViewById(R.id.recyclerHabilidades);
        recyclerUniversidades = findViewById(R.id.recyclerUniversidades);
        btnRechazar = findViewById(R.id.btnRechazar);
        btnAceptar = findViewById(R.id.btnAceptar);
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

    private void cargarDetallePostulante() {
        progressBar.setVisibility(View.VISIBLE);

        Call<DetallePostulante> call = apiService.getDetallePostulante(tokenActual, idPostulacionActual);
        call.enqueue(new Callback<DetallePostulante>() {
            @Override
            public void onResponse(Call<DetallePostulante> call, Response<DetallePostulante> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    pintarDatos(response.body());
                } else {
                    Toast.makeText(DetallePostulanteActivity.this, "No se pudo cargar el detalle del postulante.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DetallePostulante> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetallePostulanteActivity.this, "onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintarDatos(DetallePostulante detalle) {
        PerfilEstudiante perfil = detalle.getPerfil();
        Postulacion postulacion = detalle.getPostulacion();

        // --- 1. Pintar Datos del Perfil ---
        if (perfil != null) {
            if (perfil.getUsuario() != null) {
                textNombreCompleto.setText(perfil.getUsuario().getNombre() + " " + perfil.getUsuario().getApellido());
            }
            textCarrera.setText(perfil.getCarrera());
            textDescripcionPerfil.setText(perfil.getDescripcion());

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

        // --- 2. Pintar Datos de la Postulación ---
        if (postulacion != null) {
            textPropuesta.setText(postulacion.getPropuesta());
            textTiempo.setText(postulacion.getTiempoEstimado());

            Locale locale = new Locale("es", "SV");
            NumberFormat format = NumberFormat.getCurrencyInstance(locale);
            textOferta.setText(format.format(postulacion.getMontoOfertado()));
        }
    }

    private void aceptarPostulacion() {
        // Llamar al endpoint con el estado "aceptada"
        actualizarEstado("aceptada");
    }

    private void rechazarPostulacion() {
        // Llamar al endpoint con el estado "rechazada"
        actualizarEstado("rechazada");
    }

    /**
     * Método genérico para llamar a la API y actualizar el estado.
     * @param nuevoEstado El estado ("aceptada" o "rechazada")
     */
    private void actualizarEstado(String nuevoEstado) {
        progressBar.setVisibility(View.VISIBLE);
        // Deshabilitar botones para evitar doble clic
        btnAceptar.setEnabled(false);
        btnRechazar.setEnabled(false);

        Call<Void> call = apiService.actualizarEstadoPostulacion(tokenActual, idPostulacionActual, nuevoEstado);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(DetallePostulanteActivity.this, "Postulación " + nuevoEstado + " con éxito", Toast.LENGTH_SHORT).show();

                    // Cerramos esta pantalla para forzar al contratista a recargar
                    // la lista de "Ver Postulaciones" (que ahora tendrá el estado actualizado).
                    finish();
                } else {
                    Toast.makeText(DetallePostulanteActivity.this, "Error al actualizar estado", Toast.LENGTH_SHORT).show();
                    // Volver a habilitar botones si falla
                    btnAceptar.setEnabled(true);
                    btnRechazar.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(DetallePostulanteActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnAceptar.setEnabled(true);
                btnRechazar.setEnabled(true);
            }
        });

    }
}