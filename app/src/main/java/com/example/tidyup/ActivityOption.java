package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityOption extends AppCompatActivity {

    private Button botonAdultos, botonMayores, accesoAdolescentes;
    private TextView tvCerrarSesion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        accesoAdolescentes = findViewById(R.id.button);
        botonAdultos = findViewById(R.id.button2);
        botonMayores = findViewById(R.id.button3);
        tvCerrarSesion = findViewById(R.id.tvCerrarSesion);

        accesoAdolescentes.setOnClickListener(v -> actualizarRolYNavegar("adolescente", MainActivity_Adolescentes.class));
        botonAdultos.setOnClickListener(v -> actualizarRolYNavegar("adulto", MainActivity_Adultos.class));
        botonMayores.setOnClickListener(v -> actualizarRolYNavegar("mayor", MainActivity_Mayores.class));


        TextView tvBienvenida = findViewById(R.id.tvBienvenidaUsuario);
        String miUid = FirebaseManager.getCurrentUserUid();

        if (!miUid.isEmpty()) {
            FirebaseManager.obtenerUsuarioPorUid(miUid).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String nombreUsuario = doc.getString("nombre");
                    if (nombreUsuario != null) {
                        tvBienvenida.setText("¡Bienvenido, " + nombreUsuario + "!");
                    }
                }
            }).addOnFailureListener(e -> Log.e("TIDYUP", "Error al cargar el nombre de usuario", e));
        }

        tvCerrarSesion.setOnClickListener(v -> {
            FirebaseManager.cerrarSesion();
            Intent intent = new Intent(ActivityOption.this, ActivityLogin.class);
            startActivity(intent);

            finish();

            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void actualizarRolYNavegar(String nuevoRol, Class<?> claseDestino) {
        if (!FirebaseManager.getCurrentUserUid().isEmpty()) {
            FirebaseManager.actualizarRolUsuario(nuevoRol)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(ActivityOption.this, claseDestino);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ActivityOption.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
        }
    }
}