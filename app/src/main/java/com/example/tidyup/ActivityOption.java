package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ActivityOption extends AppCompatActivity {

    private Button botonAdultos, botonMayores, accesoAdolescentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        accesoAdolescentes = findViewById(R.id.button);
        botonAdultos = findViewById(R.id.button2);
        botonMayores = findViewById(R.id.button3);

        accesoAdolescentes.setOnClickListener(v -> actualizarRolYNavegar("adolescente", ActivityTest.class));
        botonAdultos.setOnClickListener(v -> actualizarRolYNavegar("adulto", ActivityTest.class));
        botonMayores.setOnClickListener(v -> actualizarRolYNavegar("mayor", ActivityTest.class));
    }

    private void actualizarRolYNavegar(String nuevoRol, Class<?> claseDestino) {
        // Miramos si hay un usuario logueado en el Manager
        if (!FirebaseManager.getCurrentUserUid().isEmpty()) {

            // Le pedimos al Manager que actualice el rol
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