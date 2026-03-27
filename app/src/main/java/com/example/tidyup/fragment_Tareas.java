package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class fragment_Tareas extends Fragment {

    public fragment_Tareas() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Enlazamos con el diseño XML de la lista de tareas
        View view = inflater.inflate(R.layout.fragment__tareas, container, false);

        // 2. Buscamos el botón amarillo por el ID que tienes en tu XML (button2)
        Button btnNuevaTarea = view.findViewById(R.id.button2);

        // 3. Le decimos qué hacer al pulsarlo
        btnNuevaTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hacemos el viaje al Fragment de Crear Tarea
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.contenedor_fragments, new CrearTareaFragment())
                        .addToBackStack(null) // Permite volver atrás con el botón del móvil
                        .commit();
            }
        });

        return view;
    }
}