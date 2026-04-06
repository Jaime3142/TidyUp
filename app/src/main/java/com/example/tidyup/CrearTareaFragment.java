package com.example.tidyup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// IMPORTS DE FIREBASE
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CrearTareaFragment extends Fragment {

    private Spinner spinnerUsuario;
    private EditText etFecha;
    private EditText etNombreTarea;
    private EditText etDescripcionTarea; // <-- Nuestro nuevo campo
    private Button btnCrear;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaUsuarios;

    // Referencia a tu base de datos Firestore
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anadir_atarea, container, false);

        // Inicializamos Firestore
        db = FirebaseFirestore.getInstance();

        // 1. Enlazar XML
        spinnerUsuario = view.findViewById(R.id.spinnerUsuario);
        etFecha = view.findViewById(R.id.etFecha);
        etNombreTarea = view.findViewById(R.id.etNombreTarea);
        etDescripcionTarea = view.findViewById(R.id.etDescripcionTarea); // <-- Enlazado
        btnCrear = view.findViewById(R.id.btnCrear);

        // 2. Preparar el Spinner
        listaUsuarios = new ArrayList<>();
        listaUsuarios.add("Cargando usuarios...");
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaUsuarios);
        spinnerUsuario.setAdapter(adapter);

        // Llamamos a Firebase para traer los usuarios reales
        cargarUsuariosDeFirebase();

        // 3. Configurar el Calendario
        etFecha.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            int anio = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        // Formateamos la fecha (ej: "2/2/2027")
                        String fechaElegida = dayOfMonth + "/" + (month + 1) + "/" + year;
                        etFecha.setText(fechaElegida);
                    }, anio, mes, dia);
            datePickerDialog.show();
        });

        // 4. Botón Crear Tarea
        btnCrear.setOnClickListener(v -> {
            String tarea = etNombreTarea.getText().toString().trim();
            String usuarioAsignado = spinnerUsuario.getSelectedItem().toString();
            String fecha = etFecha.getText().toString().trim();
            String descripcion = etDescripcionTarea.getText().toString().trim(); // <-- Recogemos el texto

            if (tarea.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, rellena los campos principales", Toast.LENGTH_SHORT).show();
            } else if (usuarioAsignado.equals("Cargando usuarios...") || usuarioAsignado.equals("Selecciona usuario...")) {
                Toast.makeText(getContext(), "Selecciona un usuario válido", Toast.LENGTH_SHORT).show();
            } else {
                // 🔥 Pasamos los 4 datos (incluida la descripción) a la función
                guardarTareaEnFirebase(tarea, usuarioAsignado, fecha, descripcion);
            }
        });

        return view;
    }

    private void cargarUsuariosDeFirebase() {
        // Buscamos en la colección "Usuarios"
        db.collection("Usuarios").get().addOnCompleteListener(task -> {
            listaUsuarios.clear();
            listaUsuarios.add("Selecciona usuario...");

            if (task.isSuccessful()) {
                // Si la conexión fue bien, leemos todos los documentos de usuarios
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String emailUsuario = document.getString("email");
                    if (emailUsuario != null) {
                        listaUsuarios.add(emailUsuario);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar los usuarios", Toast.LENGTH_SHORT).show();
            }
            // Refrescamos la lista visual
            adapter.notifyDataSetChanged();
        });
    }

    // 🔥 La función ahora recibe el String de la descripción
    private void guardarTareaEnFirebase(String nombreTarea, String usuario, String fecha, String descripcion) {
        // Creamos un "mapa" con los datos exactos
        Map<String, Object> nuevaTarea = new HashMap<>();
        nuevaTarea.put("titulo", nombreTarea);
        nuevaTarea.put("descripcion", descripcion); // 🔥 Guardamos lo que se ha escrito
        nuevaTarea.put("asignada", usuario);
        nuevaTarea.put("fecha", fecha);
        nuevaTarea.put("estado", "pendiente");
        nuevaTarea.put("puntos", 100);

        // Guardamos en la colección "Tareas"
        db.collection("Tareas")
                .add(nuevaTarea)
                .addOnSuccessListener(documentReference -> {
                    // Si sale bien
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "¡Tarea creada con éxito!", Toast.LENGTH_SHORT).show();

                        // 🔥 Limpiamos TODOS los campos para poder crear otra
                        etNombreTarea.setText("");
                        etDescripcionTarea.setText("");
                        etFecha.setText("");
                        spinnerUsuario.setSelection(0);
                    }
                })
                .addOnFailureListener(e -> {
                    // Si hay error (ej. sin internet o sin permisos)
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: No se pudo guardar la tarea", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}