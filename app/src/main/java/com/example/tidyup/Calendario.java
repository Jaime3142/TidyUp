package com.example.tidyup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
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

public class Calendario extends Fragment {

    private String emailUsuarioLogueado = "";
    private FirebaseFirestore db;

    private ListView listViewTasks;
    private ArrayList<MiTarea> listaTareas;
    private TareasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendario, container, false);

        TextView tvTitulo = view.findViewById(R.id.tvTitle);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        listViewTasks = view.findViewById(R.id.listViewTasks);

        listaTareas = new ArrayList<>();
        adapter = new TareasAdapter();
        listViewTasks.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            emailUsuarioLogueado = user.getEmail();
            tvTitulo.setText("Cargando...");

            // 1. Cargar Nombre Usuario desde Firebase
            db.collection("Usuarios").whereEqualTo("email", emailUsuarioLogueado).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String nombreReal = document.getString("nombre");
                        tvTitulo.setText((nombreReal != null && !nombreReal.isEmpty()) ? nombreReal : emailUsuarioLogueado);
                        break;
                    }
                } else {
                    tvTitulo.setText(emailUsuarioLogueado);
                }
            });

            // 2. Cargar tareas de Hoy al iniciar la app
            Calendar cal = Calendar.getInstance();
            String fechaHoy = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
            buscarTareasPorFecha(fechaHoy);

            // 3. Escuchar clics en el calendario
            calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                buscarTareasPorFecha(fechaSeleccionada);
            });

        } else {
            tvTitulo.setText("Invitado");
        }

        return view;
    }

    private void buscarTareasPorFecha(String fecha) {
        if (emailUsuarioLogueado.isEmpty() || getContext() == null) return;

        // Mostrar mensaje de carga mientras busca
        listaTareas.clear();
        listaTareas.add(new MiTarea("", "Buscando tareas...", "", ""));
        adapter.notifyDataSetChanged();

        db.collection("Tareas")
                .whereEqualTo("asignada", emailUsuarioLogueado)
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnCompleteListener(task -> {
                    listaTareas.clear();

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            // Si no hay tareas para ese día
                            listaTareas.add(new MiTarea("", "No hay tareas para este día 🎉", "", ""));
                        } else {
                            // Si encuentra tareas, las añade a la lista
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String idDoc = document.getId(); // Necesario para actualizar luego
                                String titulo = document.getString("titulo");
                                String descripcion = document.getString("descripcion");
                                String estado = document.getString("estado");

                                if(descripcion == null) descripcion = "";
                                if(estado == null) estado = "pendiente";

                                listaTareas.add(new MiTarea(idDoc, titulo, descripcion, estado));
                            }
                        }
                    } else {
                        Log.e("Calendario", "Error al conectar con Firebase", task.getException());
                        listaTareas.add(new MiTarea("", "Error al cargar las tareas", "", ""));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // 1. Clase MiTarea
    private class MiTarea {
        String idDocumento;
        String titulo;
        String descripcion;
        String estado;

        MiTarea(String idDocumento, String titulo, String descripcion, String estado) {
            this.idDocumento = idDocumento;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.estado = estado;
        }
    }

    // 2. TareasAdapter
    private class TareasAdapter extends ArrayAdapter<MiTarea> {
        TareasAdapter() {
            super(requireContext(), R.layout.tareas_del_dia, listaTareas);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.tareas_del_dia, parent, false);
            }

            MiTarea tareaActual = getItem(position);

            TextView tvNombreTarea = convertView.findViewById(R.id.tvTaskTitle);
            TextView tvDescripcionTarea = convertView.findViewById(R.id.tvTaskTime);
            CheckBox cbTarea = convertView.findViewById(R.id.checkBoxTask);

            if (tvNombreTarea != null) tvNombreTarea.setText(tareaActual.titulo);
            if (tvDescripcionTarea != null) tvDescripcionTarea.setText(tareaActual.descripcion);

            if (cbTarea != null) {
                // Si la tarea es un mensaje de sistema ("No hay tareas", "Buscando..."), ocultamos el CheckBox
                if (tareaActual.idDocumento == null || tareaActual.idDocumento.isEmpty()) {
                    cbTarea.setVisibility(View.GONE);
                } else {
                    cbTarea.setVisibility(View.VISIBLE);

                    // 1. Quitamos temporalmente el listener para que no se dispare al cargar la lista
                    cbTarea.setOnCheckedChangeListener(null);

                    // 2. Marcamos la casilla si en Firebase dice "completada"
                    cbTarea.setChecked(tareaActual.estado.equalsIgnoreCase("completada"));

                    // 3. Si el usuario toca el CheckBox, actualizamos Firebase al instante
                    cbTarea.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        String nuevoEstado = isChecked ? "completada" : "pendiente";

                        // Actualizamos el objeto local
                        tareaActual.estado = nuevoEstado;

                        // Actualizamos en Firebase usando el ID del documento
                        db.collection("Tareas").document(tareaActual.idDocumento)
                                .update("estado", nuevoEstado)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Calendario", "Tarea actualizada a: " + nuevoEstado);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Calendario", "Error actualizando tarea", e);
                                    // Si falla (ej. sin internet), devolvemos la cajita a como estaba visualmente
                                    cbTarea.setOnCheckedChangeListener(null);
                                    cbTarea.setChecked(!isChecked);
                                    tareaActual.estado = !isChecked ? "completada" : "pendiente";
                                    // Volvemos a poner el listener después de revertir
                                    notifyDataSetChanged();
                                });
                    });
                }
            }

            return convertView;
        }
    }
}