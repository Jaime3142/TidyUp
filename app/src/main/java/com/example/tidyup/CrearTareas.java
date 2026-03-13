package com.example.tidyup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class CrearTareas extends AppCompatActivity {

    private EditText etNombreTarea, etFecha;
    private Spinner spinnerUsuario;
    private Button btnCrear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_tareas);

        etNombreTarea = findViewById(R.id.tituloTarea);
        spinnerUsuario = findViewById(R.id.nUsuario);
        etFecha = findViewById(R.id.fecha);
        btnCrear = findViewById(R.id.bCrearT);

        // Configurar campo de fecha para que no se abra el teclado
        etFecha.setFocusable(false);
        etFecha.setClickable(true);

        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorDeFecha();
            }
        });

        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombreTarea.getText().toString();
                String fecha = etFecha.getText().toString();

                if (nombre.isEmpty() || fecha.isEmpty()) {
                    Toast.makeText(CrearTareas.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CrearTareas.this, "¡Tarea Creada!", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra esta pantalla y vuelve al Calendario
                }
            }
        });
    }

    private void mostrarSelectorDeFecha() {
        Calendar cal = Calendar.getInstance();
        int anio = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(CrearTareas.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String fechaElegida = dayOfMonth + "/" + (month + 1) + "/" + year;
                etFecha.setText(fechaElegida);
            }
        }, anio, mes, dia);

        dialog.show();
    }
}