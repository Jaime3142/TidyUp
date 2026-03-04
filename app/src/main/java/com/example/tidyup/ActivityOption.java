package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityOption extends AppCompatActivity {

    public Button accesoAdolescentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        accesoAdolescentes=findViewById(R.id.AccesoAdolescentes);

        accesoAdolescentes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irAdolescentes = new Intent(ActivityOption.this, MainActivity_Adolescentes.class);
                startActivity(irAdolescentes);

            }
        });
    }



}