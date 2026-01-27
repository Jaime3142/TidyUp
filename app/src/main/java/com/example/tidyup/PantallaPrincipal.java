package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;  // ¡IMPORTANTE!
import android.os.Bundle;

// ¡ESTA LÍNEA ES CLAVE!
public class PantallaPrincipal extends AppCompatActivity {  // Debe extender AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Cambia esto si tu layout tiene otro nombre
    }
}