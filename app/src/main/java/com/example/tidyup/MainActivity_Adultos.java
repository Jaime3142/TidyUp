package com.example.tidyup;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity_Adultos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragmentSeleccionado = null;
                int itemId = item.getItemId();

                // Cuando tocas el icono de la Casita carga el Calendario
                if (itemId == R.id.nav_home) {
                    fragmentSeleccionado = new Calendario();
                }
                else if (itemId == R.id.nav_notificaciones) {
                    fragmentSeleccionado = new Fragment_notificaciones();
                }
                else if (itemId == R.id.nav_grupos) {
                    fragmentSeleccionado = new FragmentGroups();
                }
                else if (itemId == R.id.nav_tareas) {
                    fragmentSeleccionado = new fragment_Tareas();
                }
                else if (itemId == R.id.nav_grafico) {
                    fragmentSeleccionado = new Fragment_grafico();
                }

                if (fragmentSeleccionado != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contenedor_fragments, fragmentSeleccionado)
                            .commit();
                }
                return true;
            }
        });

        // Nada más entrar a la app, carga el Calendario directamente
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_fragments, new Calendario())
                    .commit();
            bottomNav.setSelectedItemId(R.id.nav_home); // Esto ilumina la casita
        }
    }
}