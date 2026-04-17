package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class ActivitySignUp extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

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
        // 1. Llamamos al Manager
        FirebaseManager.registrarUsuarioAuth(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // 2. Si la Auth sale bien, creamos el perfil en la Database
                    FirebaseManager.crearPerfilUsuario(uid, username, email)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "¡Usuario creado correctamente!", Toast.LENGTH_LONG).show();
                                irALogin();
                            })
                            .addOnFailureListener(e -> {
                                // Error al guardar en la base de datos (ej. falta de permisos o internet)
                                Toast.makeText(getApplicationContext(), "Error Database: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // gestionamos los errores específicos de Registro (Auth)
                    String mensajeError = "Error en el registro";

                    if (e instanceof FirebaseAuthWeakPasswordException) {
                        mensajeError = "La contraseña es muy débil (mínimo 6 caracteres)";
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        mensajeError = "El formato del correo es inválido";
                    } else {
                        mensajeError = "Este correo ya está registrado";
                    }

                    Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show();
                });
    }

    private void irALogin() {
        Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}