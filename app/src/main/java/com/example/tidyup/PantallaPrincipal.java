package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PantallaPrincipal extends AppCompatActivity {

    private CardView cardCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Encontramos la tarjeta del calendario
        cardCalendar = findViewById(R.id.cardCalendar);

        // 2. Le ponemos un evento de clic
        cardCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // INTENT: Viajar de PantallaPrincipal a CalendarioActivity
                Intent intent = new Intent(PantallaPrincipal.this, CalendarioActivity.class);
                startActivity(intent);
            }
        });
    }
}