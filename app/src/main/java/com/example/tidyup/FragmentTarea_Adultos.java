package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.List;

public class FragmentTarea_Adultos extends Fragment {

    private Button agregarTarea;
    private LinearLayout contenedor;
    FirebaseManager manager = new FirebaseManager();

    // Constructor corregido
    public FragmentTarea_Adultos() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflamos el XML fragment__tareas
        View rootView = inflater.inflate(R.layout.fragment__tareas, container, false);

        // Referencias del XML del fragmento
        contenedor = rootView.findViewById(R.id.Lista);
        agregarTarea = rootView.findViewById(R.id.abrirFormulario);

        // Carga de tareas desde Firebase
        cargarTareas(inflater);

        // BOTÓN AGREGAR: Ahora lleva a CrearTareaFragment
        agregarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LLAMAMOS AL NUEVO FRAGMENTO
                reemplazarFragment(new CrearTareaFragment());
            }
        });

        return rootView;
    }

    private void cargarTareas(LayoutInflater inflater) {
        manager.obtenerCorreosDelGrupoActual(new FirebaseManager.CorreosCallback() {
            @Override
            public void onCorreosLoaded(List<String> correos) {
                manager.cargarTareasDelGrupoEnContenedor(contenedor, inflater, correos, task -> {
                    if (!task.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reemplazarFragment(Fragment fragment) {
        // ID CORREGIDO: Usamos contenedor_fragments que es el que está en tu ActivityMain
        getParentFragmentManager().beginTransaction()
                .replace(R.id.contenedor_fragments, fragment)
                .addToBackStack(null)
                .commit();
    }
}