package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ActivityOption extends AppCompatActivity {

    private FragmentContainerView fg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mayores);
        fg = findViewById(R.id.fragmentContainerView);

        // --- IMPLEMENTACIÓN DEL BOTÓN HOME ---
        // 1. Buscamos el botón de la casita (Home)
        ImageButton btnHome = findViewById(R.id.imageButton4);

        // 2. Al hacer clic, usamos TU función reemplazarContainer
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamamos a tu método con el fragmento de los recordatorios
                reemplazarContainer(new Recordatorio1());
            }
        });
        // ---------------------------------------
    }

    public void reemplazarContainer(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
    }
}