package com.example.tidyup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrearTareaFragment extends Fragment {

    private Spinner spinnerUsuario;
    private EditText etFecha;
    private EditText etNombreTarea;
    private EditText etDescripcionTarea;
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
        etDescripcionTarea = view.findViewById(R.id.etDescripcionTarea);
        btnCrear = view.findViewById(R.id.btnCrear);

        // 2. Preparar el Spinner
        listaUsuarios = new ArrayList<>();
        listaUsuarios.add("Cargando usuarios...");
        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaUsuarios);
        spinnerUsuario.setAdapter(adapter);

        cargarUsuariosCompañeros();

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
            String descripcion = etDescripcionTarea.getText().toString().trim();

            if (tarea.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, rellena los campos principales", Toast.LENGTH_SHORT).show();
            } else if (usuarioAsignado.equals("Cargando usuarios...") || usuarioAsignado.equals("Selecciona usuario...")) {
                Toast.makeText(getContext(), "Selecciona un usuario válido", Toast.LENGTH_SHORT).show();
            } else {
                guardarTareaEnFirebase(tarea, usuarioAsignado, fecha, descripcion);
            }
        });

        return view;
    }

    private void cargarUsuariosCompañeros() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || getContext() == null) return;

        String miUid = currentUser.getUid();

        // PASO 1: Buscamos los grupos donde estoy yo
        db.collection("Grupos")
                .whereArrayContains("miembros", miUid)
                .get()
                .addOnCompleteListener(taskGrupos -> {
                    if (taskGrupos.isSuccessful() && taskGrupos.getResult() != null) {

                        Set<String> uidsCompañeros = new HashSet<>();

                        // Recolectamos UIDs de los compañeros
                        for (QueryDocumentSnapshot grupoDoc : taskGrupos.getResult()) {
                            List<String> miembrosDelGrupo = (List<String>) grupoDoc.get("miembros");
                            if (miembrosDelGrupo != null) {
                                uidsCompañeros.addAll(miembrosDelGrupo);
                            }
                        }

                        // PASO 2: Cruzamos esos UIDs con la colección Usuarios para sacar los emails
                        db.collection("Usuarios").get().addOnCompleteListener(taskUsuarios -> {

                            // Limpiamos la lista y ponemos el título por defecto
                            listaUsuarios.clear();
                            listaUsuarios.add("Selecciona usuario...");

                            if (taskUsuarios.isSuccessful() && taskUsuarios.getResult() != null) {
                                for (QueryDocumentSnapshot usuarioDoc : taskUsuarios.getResult()) {
                                    String uidUsuario = usuarioDoc.getId();

                                    // Si el UID está en nuestra lista de compañeros (o somos nosotros mismos)
                                    if (uidsCompañeros.contains(uidUsuario)) {
                                        String email = usuarioDoc.getString("email");
                                        if (email != null) {
                                            listaUsuarios.add(email);
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(getContext(), "Error al cargar los emails", Toast.LENGTH_SHORT).show();
                            }
                            // Avisamos al Spinner de que los datos están listos
                            adapter.notifyDataSetChanged();
                        });

                    } else {
                        Toast.makeText(getContext(), "Error al buscar los grupos", Toast.LENGTH_SHORT).show();
                        Log.e("CrearTarea", "Error:", taskGrupos.getException());
                    }
                });
    }

    private void guardarTareaEnFirebase(String nombreTarea, String usuario, String fecha, String descripcion) {
        Map<String, Object> nuevaTarea = new HashMap<>();
        nuevaTarea.put("titulo", nombreTarea);
        nuevaTarea.put("descripcion", descripcion);
        nuevaTarea.put("asignada", usuario);
        nuevaTarea.put("fecha", fecha);
        nuevaTarea.put("estado", "pendiente");
        nuevaTarea.put("puntos", 100);

        db.collection("Tareas")
                .add(nuevaTarea)
                .addOnSuccessListener(documentReference -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "¡Tarea creada con éxito!", Toast.LENGTH_SHORT).show();

                        etNombreTarea.setText("");
                        etDescripcionTarea.setText("");
                        etFecha.setText("");
                        spinnerUsuario.setSelection(0);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: No se pudo guardar la tarea", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}