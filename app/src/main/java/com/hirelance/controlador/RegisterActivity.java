package com.hirelance.controlador;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hirelance.R;
import com.hirelance.modelo.Usuario;
import com.hirelance.red.ApiService;
import com.hirelance.red.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Vistas
    private TextInputLayout layoutInputNombre, layoutInputApellido, layoutInputCorreo,
            layoutInputContrasena, layoutInputConfirmarContrasena,
            layoutInputDui, layoutInputTelefono;
    private TextInputEditText editNombre, editApellido, editCorreo,
            editContrasena, editConfirmarContrasena,
            editDui, editTelefono;
    private RadioGroup radioGroupTipoUsuario;
    private RadioButton radioEstudiante, radioContratista;
    private MaterialButton botonRegistrarse;
    private TextView textIrALogin;
    private ProgressBar progressBarRegistro;

    // API
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Inicializar API
        apiService = RetrofitClient.getClient().create(ApiService.class);

        // 2. Vincular Vistas
        vincularVistas();

        // 3. Configurar Listeners
        configurarListeners();
    }

    private void vincularVistas() {
        // Layouts
        layoutInputNombre = findViewById(R.id.layoutInputNombre);
        layoutInputApellido = findViewById(R.id.layoutInputApellido);
        layoutInputCorreo = findViewById(R.id.layoutInputCorreo);
        layoutInputContrasena = findViewById(R.id.layoutInputContrasena);
        layoutInputConfirmarContrasena = findViewById(R.id.layoutInputConfirmarContrasena);
        layoutInputDui = findViewById(R.id.layoutInputDui);
        layoutInputTelefono = findViewById(R.id.layoutInputTelefono);

        // EditTexts
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        editCorreo = findViewById(R.id.editCorreo);
        editContrasena = findViewById(R.id.editContrasena);
        editConfirmarContrasena = findViewById(R.id.editConfirmarContrasena);
        editDui = findViewById(R.id.editDui);
        editTelefono = findViewById(R.id.editTelefono);

        // Radio
        radioGroupTipoUsuario = findViewById(R.id.radioGroupTipoUsuario);
        radioEstudiante = findViewById(R.id.radioEstudiante);
        radioContratista = findViewById(R.id.radioContratista);

        // Botones y otros
        botonRegistrarse = findViewById(R.id.botonRegistrarse);
        textIrALogin = findViewById(R.id.textIrALogin);
        progressBarRegistro = findViewById(R.id.progressBarRegistro);
    }

    private void configurarListeners() {
        botonRegistrarse.setOnClickListener(v -> {
            intentarRegistro();
        });

        textIrALogin.setOnClickListener(v -> {
            // Cierra esta actividad y regresa a la anterior (LoginActivity)
            finish();
        });
    }

    private void intentarRegistro() {
        // 1. Validar el formulario
        if (!validarFormulario()) {
            Toast.makeText(this, "Por favor, corrige los errores en el formulario", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Mostrar ProgressBar y desactivar botón
        progressBarRegistro.setVisibility(View.VISIBLE);
        botonRegistrarse.setEnabled(false);

        // 3. Recolectar los datos y crear el objeto Usuario
        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String correo = editCorreo.getText().toString().trim();
        String contrasena = editContrasena.getText().toString().trim();
        String dui = editDui.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();

        // Obtener el tipo de usuario del RadioGroup
        String tipoUsuario;
        int selectedId = radioGroupTipoUsuario.getCheckedRadioButtonId();
        if (selectedId == R.id.radioEstudiante) {
            tipoUsuario = "estudiante";
        } else {
            tipoUsuario = "contratista";
        }

        // Creamos el objeto Usuario usando el constructor de la Parte 1
        Usuario nuevoUsuario = new Usuario(nombre, apellido, correo, contrasena, dui, tipoUsuario, telefono);

        // 4. Hacer la llamada a la API
        Call<Usuario> call = apiService.register(nuevoUsuario);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                // Ocultar ProgressBar y reactivar botón
                progressBarRegistro.setVisibility(View.GONE);
                botonRegistrarse.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    // ¡Éxito!
                    Toast.makeText(RegisterActivity.this, R.string.exito_registro, Toast.LENGTH_LONG).show();
                    // Regresamos a la pantalla de Login
                    finish();
                } else {
                    // Error del servidor (ej. 409 Conflict - correo ya existe)
                    try {
                        // Intentamos leer el mensaje de error del servidor
                        String errorBody = response.errorBody().string();
                        // (Aquí podrías parsear 'errorBody' si la API lo envía en JSON)
                        Toast.makeText(RegisterActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error en el registro. El correo podría ya estar en uso.", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Usuario> call, Throwable t) {
                // Error de red
                progressBarRegistro.setVisibility(View.GONE);
                botonRegistrarse.setEnabled(true);
                Toast.makeText(RegisterActivity.this, R.string.error_red, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Valida todos los campos del formulario de registro.
     * Muestra errores en los TextInputLayouts si es necesario.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private boolean validarFormulario() {
        // Limpiamos errores previos
        layoutInputNombre.setError(null);
        layoutInputApellido.setError(null);
        layoutInputCorreo.setError(null);
        layoutInputContrasena.setError(null);
        layoutInputConfirmarContrasena.setError(null);
        layoutInputDui.setError(null);
        layoutInputTelefono.setError(null);
        // (No hay error para RadioGroup, pero lo validamos)

        boolean esValido = true;

        String nombre = editNombre.getText().toString().trim();
        String apellido = editApellido.getText().toString().trim();
        String correo = editCorreo.getText().toString().trim();
        String contrasena = editContrasena.getText().toString().trim();
        String confirmarContrasena = editConfirmarContrasena.getText().toString().trim();
        String dui = editDui.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        int tipoSeleccionado = radioGroupTipoUsuario.getCheckedRadioButtonId();

        // Validar Nombre
        if (TextUtils.isEmpty(nombre)) {
            layoutInputNombre.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        }

        // Validar Apellido
        if (TextUtils.isEmpty(apellido)) {
            layoutInputApellido.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        }

        // Validar Correo
        if (TextUtils.isEmpty(correo)) {
            layoutInputCorreo.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            layoutInputCorreo.setError(getString(R.string.error_corre_invalido));
            esValido = false;
        }

        // Validar Contraseña
        if (TextUtils.isEmpty(contrasena)) {
            layoutInputContrasena.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        } else if (contrasena.length() < 6) {
            // (Asumimos una regla de 6 caracteres, la API debería reforzarla)
            layoutInputContrasena.setError("Debe tener al menos 6 caracteres");
            esValido = false;
        }

        // Validar Confirmar Contraseña
        if (TextUtils.isEmpty(confirmarContrasena)) {
            layoutInputConfirmarContrasena.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        } else if (!contrasena.equals(confirmarContrasena)) {
            layoutInputConfirmarContrasena.setError(getString(R.string.error_contrasenas_no_coinciden));
            esValido = false;
        }

        // Validar DUI (requerido por la BD: NOT NULL)
        if (TextUtils.isEmpty(dui)) {
            layoutInputDui.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        }

        // Validar Teléfono (opcional en la BD, pero podemos hacerlo requerido en la App)
        // (Tu BD lo tiene como 'telefono VARCHAR(20)' sin NOT NULL, así que es opcional.
        // Lo haremos requerido en la app para un mejor perfil)
        if (TextUtils.isEmpty(telefono)) {
            layoutInputTelefono.setError(getString(R.string.error_campo_vacio));
            esValido = false;
        }

        // Validar Tipo de Usuario
        if (tipoSeleccionado == -1) { // -1 significa que ninguno está seleccionado
            // No tenemos un TextInputLayout, así que usamos un Toast
            Toast.makeText(this, "Debes seleccionar si eres Estudiante o Contratista", Toast.LENGTH_SHORT).show();
            esValido = false;
        }

        return esValido;
    }
}