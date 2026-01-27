package com.example.tidyup;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private ListView listViewTasks;
    private TextView tvDateSelected;

    private ArrayList<Tarea> listaDeTareas;
    private ArrayAdapter<Tarea> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendario);

        // 1. Vincular Vistas (Ya no buscamos el botón)
        calendarView = findViewById(R.id.calendarView);
        listViewTasks = findViewById(R.id.listViewTasks);
        tvDateSelected = findViewById(R.id.tvDateSelected);

        // 2. Datos iniciales
        listaDeTareas = new ArrayList<>();
        listaDeTareas.add(new Tarea("Bienvenido a TidyUp", "Hoy"));
        listaDeTareas.add(new Tarea("Desliza para ver más", "Siempre"));

        // Añadimos muchas tareas iniciales para que pruebes el scroll
        for (int i = 1; i <= 5; i++) {
            listaDeTareas.add(new Tarea("Tarea de ejemplo " + i, "10:00 AM"));
        }

        // 3. Configurar el Adaptador
        adapter = new ArrayAdapter<Tarea>(this, R.layout.tareas_del_dia, listaDeTareas) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.tareas_del_dia, parent, false);
                }

                Tarea tareaActual = listaDeTareas.get(position);
                TextView tvTitulo = convertView.findViewById(R.id.tvTaskTitle);
                TextView tvHora = convertView.findViewById(R.id.tvTaskTime);

                if (tareaActual != null) {
                    tvTitulo.setText(tareaActual.getNombre());
                    tvHora.setText(tareaActual.getHora());
                }
                return convertView;
            }
        };

        listViewTasks.setAdapter(adapter);

        // 4. Lógica del Calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvDateSelected.setText("Tareas del: " + fecha);

            listaDeTareas.clear();

            // Simulación: Llenamos la lista con MUCHAS tareas para ver el scroll
            if (dayOfMonth % 2 == 0) {
                for (int i = 1; i <= 15; i++) {
                    listaDeTareas.add(new Tarea("Día Par - Tarea " + i, (8 + i) + ":00"));
                }
            } else {
                for (int i = 1; i <= 12; i++) {
                    listaDeTareas.add(new Tarea("Día Impar - Actividad " + i, (9 + i) + ":30"));
                }
            }

            adapter.notifyDataSetChanged();
            // Opcional: Volver al principio de la lista al cambiar de día
            listViewTasks.setSelection(0);
        });
    }
}