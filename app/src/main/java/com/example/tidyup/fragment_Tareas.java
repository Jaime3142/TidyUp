package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class fragment_Tareas extends Fragment {

    private Button agregarTarea;

    public fragment_Tareas() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__tareas, container, false);

        // ID "Lista" debe ser el LinearLayout dentro del ScrollView
        LinearLayout contenedor = rootView.findViewById(R.id.Lista);
        agregarTarea = rootView.findViewById(R.id.abrirFormulario);

        if (agregarTarea != null) {
            agregarTarea.setOnClickListener(v -> reemplazarFragment(new Fragment_CrearTAdolescentes()));
        }

        // Cargar tareas del usuario actual
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String correoActual = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            FirebaseManager.cargarTareasEnContenedor(contenedor, inflater, correoActual, task -> {
                if (!task.isSuccessful()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        return rootView;
    }

    private void reemplazarFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContiner, fragment)
                .addToBackStack(null)
                .commit();
    }
}