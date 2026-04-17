package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class CalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvDateSelected;
    private ListView listViewTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendario);

        calendarView = findViewById(R.id.calendarView);
        tvDateSelected = findViewById(R.id.tvDateSelected);
        listViewTasks = findViewById(R.id.listViewTasks);

        // Configurar fecha actual por defecto
        Calendar cal = Calendar.getInstance();
        String fechaHoy = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
        tvDateSelected.setText("Tareas del día (" + fechaHoy + ")");

        // Evento al tocar el calendario
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                tvDateSelected.setText("Tareas del día (" + fechaSeleccionada + ")");
            }
        });


        ;
    }
}