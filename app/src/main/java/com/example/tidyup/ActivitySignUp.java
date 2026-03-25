package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ActivitySignUp extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    private static final String TAG = "TIDYUP_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.editTextTextUsername);
        etEmail = findViewById(R.id.editTextTextEmailAddress);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnRegister = findViewById(R.id.buttonSignUp);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(ActivitySignUp.this, "Faltan campos", Toast.LENGTH_SHORT).show();
            } else {
                registrarUsuario(email, pass, username);
            }
        });
    }

    private void registrarUsuario(String email, String password, final String username) {
        // Llamamos al Manager para la Auth
        FirebaseManager.registrarUsuarioAuth(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Si la Auth va bien, llamamos al Manager para crear el perfil
                    FirebaseManager.crearPerfilUsuario(uid, username, email)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "¡Usuario creado correctamente!", Toast.LENGTH_LONG).show();
                                irALogin();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error Database: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Fallo Auth: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void irALogin() {
        Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}