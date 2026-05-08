package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class CalendarioActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView tvDateSelected;
    private LinearLayout contenedorTareas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.calendario);

            calendarView = findViewById(R.id.calendarView);
            tvDateSelected = findViewById(R.id.tvDateSelected);
            contenedorTareas = findViewById(R.id.contenedorTareasCalendario);

            // Configurar fecha actual por defecto
            Calendar cal = Calendar.getInstance();
            String fechaHoy = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);

            if (tvDateSelected != null) {
                tvDateSelected.setText("Tareas del día (" + fechaHoy + ")");
            }

            // Evento al tocar el calendario
            if (calendarView != null) {
                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        try {
                            String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                            if (tvDateSelected != null) {
                                tvDateSelected.setText("Tareas del día (" + fechaSeleccionada + ")");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(CalendarioActivity.this, "Error al actualizar la fecha", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Atrapamos cualquier error grave al inflar la vista para evitar que la app explote
            Toast.makeText(this, "Error al cargar la pantalla del calendario", Toast.LENGTH_SHORT).show();
        }
    }
}