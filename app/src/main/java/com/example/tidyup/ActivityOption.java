package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityOption extends AppCompatActivity {

    private Button botonMayores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        botonMayores = findViewById(R.id.botonmayores);

        botonMayores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irMayores = new Intent(ActivityOption.this,MainActivity_Mayores.class);
                startActivity(irMayores);
            }
        });
    }
}