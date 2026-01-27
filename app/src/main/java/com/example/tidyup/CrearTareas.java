package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class CrearTareas extends AppCompatActivity {  // ¡EXTIENDE AppCompatActivity!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_tareas);  // Tu XML se llama crear_tareas.xml
    }
}