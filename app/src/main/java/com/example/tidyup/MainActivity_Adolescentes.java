package com.example.tidyup;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MainActivity_Adolescentes extends AppCompatActivity {

    public FragmentContainerView fragmentContiner;
    public ImageButton botonHome;

    public ImageButton botonAjustes;

    public ImageButton botonNotificaciones;
    public ImageButton botonRecompensas;
    public ImageButton botonTareas;

    public ImageButton botonGrupos;

    public TextView nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_adolescentes);

        botonNotificaciones=findViewById(R.id.botonNotificaciones);
        botonRecompensas=findViewById(R.id.botonRecompensas);
        botonTareas=findViewById(R.id.botonTareas);
        botonGrupos=findViewById(R.id.botonGrupos);
        botonAjustes=findViewById(R.id.botonAjustes);
        botonHome=findViewById(R.id.botonInicio);
        nombreUsuario=findViewById(R.id.NombreUsuario);

        FirebaseManager firebaseMethods = new FirebaseManager();

        // Llamamos al método y le decimos qué hacer cuando reciba el nombre
        // 3. Llamada corregida (sin el "new" delante del método)
        firebaseMethods.obtenerNombreDesdeFirestore(new FirebaseManager.OnNombreRecuperadoListener() {
            @Override
            public void onNombreRecuperado(String nombre) {
                // Ahora ya no debería salir en rojo
                nombreUsuario.setText(nombre);
            }
        });


        if (savedInstanceState == null) {
            reemplazarFragment(new Calendario());
        }


        botonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new Calendario());
            }
        });

        botonGrupos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new FragmentGroups());
            }
        });

        botonAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new FragmentConfiguration());
            }
        });



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
