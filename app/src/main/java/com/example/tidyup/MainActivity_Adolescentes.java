package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity_Adolescentes extends AppCompatActivity {

    public FragmentContainerView fragmentContiner;

    public ImageButton botonNotificaciones;
    public ImageButton botonRecompensas;
    public ImageButton botonTareas;

    public ImageButton botonGrupos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_adolescentes);

        botonNotificaciones=findViewById(R.id.botonNotificaciones);
        botonRecompensas=findViewById(R.id.botonRecompensas);
        botonTareas=findViewById(R.id.botonTareas);
        botonGrupos=findViewById(R.id.botonGrupos);

        // paso de pantalla de notificaciones
        botonNotificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new Fragment_notificaciones());
            }
        });

        // paso de pantalla tareas
        botonTareas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reemplazarFragment(new fragment_Tareas());
            }
        });

        //paso de pantalla recompensas
        botonRecompensas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new fragment_recompensas());
            }
        });




    }
    public void reemplazarFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Reemplazo el contenido del contenedor con el nuevo fragmento
        fragmentTransaction.replace(R.id.fragmentContiner, fragment);

        fragmentTransaction.commit();
    }



    }
