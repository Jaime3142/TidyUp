package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
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

    // Ahora nuestra lista guarda objetos "MiTarea" en vez de Strings normales
    private ArrayList<MiTarea> listaTareas;
    private TareasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calendario, container, false);

        TextView tvTitulo = view.findViewById(R.id.tvTitle);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        listViewTasks = view.findViewById(R.id.listViewTasks);

        // Preparamos nuestro Adaptador Personalizado
        listaTareas = new ArrayList<>();
        adapter = new TareasAdapter();
        listViewTasks.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            emailUsuarioLogueado = user.getEmail();
            tvTitulo.setText("Cargando...");

            // 1. Cargar Nombre Usuario
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

            // 2. Cargar tareas de Hoy
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

        listaTareas.clear();
        listaTareas.add(new MiTarea("Buscando tareas...", ""));
        adapter.notifyDataSetChanged();

        db.collection("Tareas")
                .whereEqualTo("asignada", emailUsuarioLogueado)
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnCompleteListener(task -> {
                    listaTareas.clear();

                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            listaTareas.add(new MiTarea("No hay tareas para este día 🎉", ""));
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String titulo = document.getString("titulo");
                                String descripcion = document.getString("descripcion");

                                if(descripcion == null) descripcion = "";

                                // Añadimos la tarea a la lista
                                listaTareas.add(new MiTarea(titulo, descripcion));
                            }
                        }
                    } else {
                        listaTareas.add(new MiTarea("Error al conectar", ""));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // =========================================================
    // CLASES PARA EL DISEÑO PERSONALIZADO (AQUÍ ESTÁ LA MAGIA)
    // =========================================================

    // 1. Una clase sencilla para guardar los datos de una tarea
    private class MiTarea {
        String titulo, descripcion;
        MiTarea(String titulo, String descripcion) {
            this.titulo = titulo;
            this.descripcion = descripcion;
        }
    }

    // 2. Nuestro Adaptador que conecta los datos con tu diseño XML
    private class TareasAdapter extends ArrayAdapter<MiTarea> {
        TareasAdapter() {
            super(requireContext(), R.layout.tareas_del_dia, listaTareas);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Inflamos TU diseño: tareas_del_dia.xml
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.tareas_del_dia, parent, false);
            }

            MiTarea tareaActual = getItem(position);

            // 🔥 AQUÍ HEMOS PUESTO TUS IDs REALES 🔥
            TextView tvNombreTarea = convertView.findViewById(R.id.tvTaskTitle);
            TextView tvDescripcionTarea = convertView.findViewById(R.id.tvTaskTime);

            // Si encontró los textos en tu diseño, les mete la info de Firebase
            if (tvNombreTarea != null) {
                tvNombreTarea.setText(tareaActual.titulo);
            }
            if (tvDescripcionTarea != null) {
                tvDescripcionTarea.setText(tareaActual.descripcion);
            }

            return convertView;
        }
        }
    }
