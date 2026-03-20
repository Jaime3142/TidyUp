package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityOption extends AppCompatActivity {

    // Cambiamos el nombre de la variable para que sea más claro (opcional, pero buena práctica)
    private Button botonDos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        // 1. Buscamos el SEGUNDO botón en el XML por su ID (R.id.button2)
        botonDos = findViewById(R.id.button2);

        // 2. Le añadimos el OnClickListener al segundo botón
        botonDos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 3. Creamos la intención de ir a PantallaPrincipal
                Intent intent = new Intent(ActivityOption.this, PantallaPrincipal.class);

                // 4. Arrancamos la nueva pantalla
                startActivity(intent);
            }
        });
    }
}