package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PantallaPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enlazamos los CardViews del XML con variables en Java
        CardView cardCalendar = findViewById(R.id.cardCalendar);

        // 2. Le decimos qué hacer cuando se hace clic en "Calendario"
        cardCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre la pantalla del Calendario usando tu clase CalendarioActivity
                Intent intent = new Intent(PantallaPrincipal.this, CalendarioActivity.class);
                startActivity(intent);
            }
        });
    }}
