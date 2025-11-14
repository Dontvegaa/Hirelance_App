package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.hirelance.R;

public class SelectorRegistroActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MaterialButton btnSoyEstudiante, btnSoyContratista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_registro);

        // 1. Vincular Vistas
        toolbar = findViewById(R.id.toolbarSelector);
        btnSoyEstudiante = findViewById(R.id.btnSoyEstudiante);
        btnSoyContratista = findViewById(R.id.btnSoyContratista);

        // 2. Configurar Toolbar
        configurarToolbar();

        // 3. Configurar Listeners
        btnSoyEstudiante.setOnClickListener(v -> {
            // Abrir el formulario de registro de estudiante
            Intent intent = new Intent(SelectorRegistroActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSoyContratista.setOnClickListener(v -> {
            // Abrir el formulario de registro de contratista
            Intent intent = new Intent(SelectorRegistroActivity.this, RegisterContratistaActivity.class);
            startActivity(intent);
        });
    }

    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // Maneja el clic en el botón de "atrás" de la toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}