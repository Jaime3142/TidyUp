package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityOption extends AppCompatActivity {

    private Button btnAdolescentes, btnAdultos, btnMayores;
    private TextView tvBienvenida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        btnAdolescentes = findViewById(R.id.button);
        btnAdultos = findViewById(R.id.button2);
        btnMayores = findViewById(R.id.button3);
        tvBienvenida = findViewById(R.id.tvBienvenidaUsuario);

        cargarNombreUsuario();

        //Lógica de selección de roles
        btnAdolescentes.setOnClickListener(v -> actualizarRolYNavegar("adolescente", MainActivity_Adolescentes.class));
        btnAdultos.setOnClickListener(v -> actualizarRolYNavegar("adulto", MainActivity_Adultos.class));
        btnMayores.setOnClickListener(v -> actualizarRolYNavegar("mayor", MainActivity_Mayores.class));
    }

    // --- MÉTODOS AUXILIARES ---

    private void cargarNombreUsuario() {
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
    }

    private void actualizarRolYNavegar(String nuevoRol, Class<?> claseDestino) {
        if (!FirebaseManager.getCurrentUserUid().isEmpty()) {
            FirebaseManager.actualizarRolUsuario(nuevoRol)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(ActivityOption.this, claseDestino);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ActivityOption.this, "Error al actualizar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_SHORT).show();
        }
    }
}