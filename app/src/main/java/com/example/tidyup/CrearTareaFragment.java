package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CrearTareaFragment extends Fragment {

    private EditText etNombreTarea;
    private Spinner spinnerUsuario;
    private EditText etFecha;
    private Button btnCrear;

    public void AddTaskFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anadir_atarea, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vincular las variables con los IDs del XML usando la vista inflada ('view')
        etNombreTarea = view.findViewById(R.id.etNombreTarea);
        spinnerUsuario = view.findViewById(R.id.spinnerUsuario);
        etFecha = view.findViewById(R.id.etFecha);
        btnCrear = view.findViewById(R.id.btnCrear);

        // Configurar el listener del botón
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí va la lógica cuando se pulsa "Crear Tarea"
                String nombreTarea = etNombreTarea.getText().toString();
                String fecha = etFecha.getText().toString();

                // Ejemplo básico de acción
                Toast.makeText(getContext(), "Tarea: " + nombreTarea + " añadida.", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el campo de fecha
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}