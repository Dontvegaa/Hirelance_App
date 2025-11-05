package sv.edu.itca.hirelance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar la action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Handler para delay y navegación
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToLogin();
            }
        }, SPLASH_DELAY);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(splash.this, login.class);
        startActivity(intent);
        finish(); // Cerrar SplashActivity para que no vuelva atrás
    }

    @Override
    public void onBackPressed() {
        // Deshabilitar back button en splash
        // super.onBackPressed();
    }
}