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

// Nuevas importaciones de Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;

public class Calendario extends Fragment {

    private CalendarView calendarView;
    private LinearLayout contenedorTareas;
    private TextView tvDateSelected;
    private TextView tvTitle; // Añadido para el título principal
    private FirebaseManager manager = new FirebaseManager();

    public Calendario() {
        // Constructor vacío requerido
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;

        try {
            rootView = inflater.inflate(R.layout.calendario, container, false);

            // 1. Enlazamos las vistas del XML usando rootView
            calendarView = rootView.findViewById(R.id.calendarView);
            contenedorTareas = rootView.findViewById(R.id.contenedorTareasCalendario);
            tvDateSelected = rootView.findViewById(R.id.tvDateSelected);
            tvTitle = rootView.findViewById(R.id.tvTitle); // Enlazamos el título

            // --- NUEVO: PONER EL NOMBRE DEL USUARIO EN EL TÍTULO ---
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && tvTitle != null) {
                String email = currentUser.getEmail();
                if (email != null) {
                    // Cortamos el correo para mostrar solo el nombre (ej: example3@gmail.com -> example3)
                    String username = email.split("@")[0];
                    tvTitle.setText(username);
                }
            }
          

            // 2. Cargamos las tareas inicialmente
            cargarTareas(inflater);

            // Ponemos la fecha de hoy por defecto en el texto
            Calendar cal = Calendar.getInstance();
            String fechaHoy = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
            if (tvDateSelected != null) {
                tvDateSelected.setText("Tareas del día (" + fechaHoy + ")");
            }

            // 3. Configuramos qué pasa cuando el usuario toca un día diferente
            if (calendarView != null) {
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        try {
                            String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;

                            // Actualizamos el texto con la nueva fecha
                            if (tvDateSelected != null) {
                                tvDateSelected.setText("Tareas del día (" + fechaSeleccionada + ")");
                            }

                            // Limpiamos el contenedor
                            contenedorTareas.removeAllViews();

                            // Cargamos las tareas de nuevo
                            cargarTareas(inflater);

                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error al actualizar el calendario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error cargando la pantalla", Toast.LENGTH_SHORT).show();
            }
        }

        return rootView;
    }

    private void cargarTareas(LayoutInflater inflater) {
        try {
            manager.obtenerCorreosDelGrupoActual(new FirebaseManager.CorreosCallback() {
                @Override
                public void onCorreosLoaded(List<String> correos) {
                    try {
                        manager.cargarTareasDelGrupoEnContenedor(contenedorTareas, inflater, correos, task -> {
                            try {
                                if (!task.isSuccessful()) {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Error al cargar tareas del calendario", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error al mostrar las tareas", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    try {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        }
    }
}