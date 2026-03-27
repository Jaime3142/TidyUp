package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityOption extends AppCompatActivity {

    private Button botonDos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        // 1. Buscamos el SEGUNDO botón en el XML por su ID
        botonDos = findViewById(R.id.button2);

        // Busca esta parte en tu ActivityOption.java
        botonDos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EL CAMBIO ESTÁ AQUÍ:
                // Asegúrate de que dice MainActivity.class y NO MainActivity_Adultos.class
                Intent intent = new Intent(ActivityOption.this, MainActivity_Adultos.class);
                startActivity(intent);
                finish();
            }
        });
    }
}