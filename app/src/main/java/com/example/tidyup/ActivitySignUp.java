package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ActivitySignUp extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Etiqueta única para buscar en el Logcat
    private static final String TAG = "TIDYUP_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Log.d(TAG, "onCreate: Iniciando Activity");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.editTextTextUsername);
        etEmail = findViewById(R.id.editTextTextEmailAddress);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnRegister = findViewById(R.id.buttonSignUp);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(ActivitySignUp.this, "Faltan campos", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onClick: Datos validados. Iniciando registro para: " + email);
                    registrarUsuario(email, pass, username);
                }
            }
        });
    }

    private void registrarUsuario(String email, String password, final String username) {
        Log.d(TAG, "registrarUsuario: Llamando a createUserWithEmailAndPassword...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Log.d(TAG, "Auth ÉXITO: Usuario creado con UID: " + uid);

                            // Paso 2: Guardar en Firestore
                            guardarDatosEnFirestore(uid, username, email);
                        }
                    } else {
                        Log.e(TAG, "Auth FALLO: " + task.getException().getMessage());
                        Toast.makeText(this, "Fallo Auth: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void guardarDatosEnFirestore(String uid, String username, String email) {
        Log.d(TAG, "Firestore: Iniciando guardado de datos...");

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", username);
        datosUsuario.put("email", email);
        datosUsuario.put("rol", "adulto");
        datosUsuario.put("puntos", 0);
        datosUsuario.put("idGrupo", "");

        try {
            Log.d(TAG, "Firestore: Intentando acceder a la colección 'Usuarios'...");

            db.collection("Usuarios").document(uid)
                    .set(datosUsuario)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firestore ÉXITO: Documento creado correctamente en la nube.");

                        // AQUI ESTÁ TU TOAST ACTUALIZADO:
                        // Usamos getApplicationContext() para que el Toast se siga viendo aunque cambiemos de Activity
                        Toast.makeText(getApplicationContext(), "¡Usuario creado correctamente!", Toast.LENGTH_LONG).show();

                        irALogin();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore FALLO: " + e.getMessage());
                        Log.e(TAG, "Firestore CAUSA: " + e.getCause());
                        Toast.makeText(getApplicationContext(), "Error Database: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Firestore ERROR CRÍTICO (Crash evitado): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void irALogin() {
        Log.d(TAG, "Navegando a ActivityLogin");
        Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}