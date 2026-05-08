package com.example.tidyup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

public class ActivityLogin extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.editTextTextUsername);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.buttonLoginWith);
        btnSignUp = findViewById(R.id.buttonSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    etEmail.setError("Introduce tu correo");
                    return;
                }
                if (pass.isEmpty()) {
                    etPassword.setError("Introduce la contraseña");
                    return;
                }

                // Inicio de sesión en Firebase
                loginUsuario(email, pass);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!FirebaseManager.getCurrentUserUid().isEmpty()) {
            redireccionarSegunRol();
        }
    }

    private void loginUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión correcto
                            Toast.makeText(ActivityLogin.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();

                            // ¡AQUÍ ESTABA EL CAMBIO! Ahora usamos el semáforo en lugar de irAHome()
                            redireccionarSegunRol();

                        } else {
                            // Usuario no registrado o error
                            String errorMsg = "Error de autenticación";
                            if (task.getException() != null) {
                                errorMsg = "Datos incorrectos o usuario no registrado";
                            }
                            Toast.makeText(ActivityLogin.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void redireccionarSegunRol() {
        String miUid = FirebaseManager.getCurrentUserUid();

        FirebaseManager.obtenerUsuarioPorUid(miUid).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String rol = doc.getString("rol");
                Intent intent;

                // Redirigimos según el rol exacto
                if ("adolescente".equals(rol)) {
                    intent = new Intent(ActivityLogin.this, MainActivity_Adolescentes.class);
                } else if ("mayor".equals(rol)) {
                    intent = new Intent(ActivityLogin.this, MainActivity_Mayores.class);
                } else if ("adulto".equals(rol)) {
                    intent = new Intent(ActivityLogin.this, MainActivity_Adultos.class);
                } else {
                    intent = new Intent(ActivityLogin.this, ActivityOption.class);
                }

                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> {
            Intent intent = new Intent(ActivityLogin.this, ActivityOption.class);
            startActivity(intent);
            finish();
        });
    }
}