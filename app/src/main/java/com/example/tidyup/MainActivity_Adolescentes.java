package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity_Adolescentes extends AppCompatActivity {

    public FragmentContainerView fragmentContiner;

    public Button botonNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_adolescentes);

        botonNotificaciones.findViewById(R.id.botonNotifi);

        botonNotificaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new Fragment_notificaciones());
            }
        });

    }
    public void reemplazarFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Reemplazamos el contenido del contenedor con el nuevo fragmento
        fragmentTransaction.replace(R.id.fragmentContiner, new Fragment_vacio());

        // (Opcional) Guardar en el historial para volver atrás con el botón físico
        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }



    }
