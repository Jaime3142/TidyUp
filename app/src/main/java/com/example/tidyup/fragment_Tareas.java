package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class fragment_Tareas extends Fragment {

    private Button agregarTarea;
    FirebaseManager manager = new FirebaseManager();

    public fragment_Tareas() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__tareas, container, false);

        // ID "Lista" debe ser el LinearLayout dentro del ScrollView
        LinearLayout contenedor = rootView.findViewById(R.id.Lista);
        agregarTarea = rootView.findViewById(R.id.abrirFormulario);

        manager.obtenerCorreosDelGrupoActual(new FirebaseManager.CorreosCallback() {
            @Override
            public void onCorreosLoaded(List<String> correos) {

                manager.cargarTareasDelGrupoEnContenedor(contenedor, inflater, correos, task -> {
                    if (!task.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al cargar tareas del grupo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al obtener miembros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }


        });

        agregarTarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reemplazarFragment(new Fragment_CrearTAdolescentes());
            }
        });
        return rootView;
    }

    private void reemplazarFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContiner, fragment)
                .addToBackStack(null)
                .commit();
    }
}