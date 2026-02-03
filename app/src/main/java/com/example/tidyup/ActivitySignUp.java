package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivitySignUp extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    // 1. Declarar FirebaseAuth
    private FirebaseAuth mAuth;
    private static final String TAG = "ActivitySignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 2. Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

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
                    Toast.makeText(ActivitySignUp.this, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
                } else if (pass.length() < 6) {
                    Toast.makeText(ActivitySignUp.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                } else {
                    // 3. Llamar al método de registro de Firebase
                    registrarUsuario(email, pass);
                }
            }
        });
    }

    private void registrarUsuario(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(ActivitySignUp.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();

                            // Opcional: Podrías guardar el 'username' en la base de datos aquí

                            irALogin();
                        } else {
                            // Si falla el registro (ej: email mal formado o ya registrado)
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ActivitySignUp.this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void irALogin() {
        Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }
}