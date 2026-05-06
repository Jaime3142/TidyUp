package com.example.tidyup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.List;

public class Calendario extends Fragment {

    private CalendarView calendarView;
    private LinearLayout contenedorTareas;
    private TextView tvDateSelected; // Añadido para el texto de la fecha
    private FirebaseManager manager = new FirebaseManager();

    public Calendario() {
        // Constructor vacío requerido
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.calendario, container, false);

        // 1. Enlazamos las vistas del XML usando rootView
        calendarView = rootView.findViewById(R.id.calendarView);
        contenedorTareas = rootView.findViewById(R.id.contenedorTareasCalendario);
        tvDateSelected = rootView.findViewById(R.id.tvDateSelected);

        // 2. Cargamos las tareas inicialmente
        cargarTareas(inflater);

        // Ponemos la fecha de hoy por defecto en el texto
        Calendar cal = Calendar.getInstance();
        String fechaHoy = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
        if (tvDateSelected != null) {
            tvDateSelected.setText("Tareas del día (" + fechaHoy + ")");
        }

        // 3. Configuramos qué pasa cuando el usuario toca un día diferente
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;

                // Actualizamos el texto con la nueva fecha
                if (tvDateSelected != null) {
                    tvDateSelected.setText("Tareas del día (" + fechaSeleccionada + ")");
                }

                // Limpiamos el contenedor
                contenedorTareas.removeAllViews();

                // Cargamos las tareas de nuevo
                cargarTareas(inflater);
            }
        });

        return rootView;
    }

    private void cargarTareas(LayoutInflater inflater) {
        manager.obtenerCorreosDelGrupoActual(new FirebaseManager.CorreosCallback() {
            @Override
            public void onCorreosLoaded(List<String> correos) {
                manager.cargarTareasDelGrupoEnContenedor(contenedorTareas, inflater, correos, task -> {
                    if (!task.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al cargar tareas del calendario", Toast.LENGTH_SHORT).show();
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
}