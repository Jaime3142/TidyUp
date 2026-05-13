package com.example.tidyup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ActivityLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.editTextTextUsername);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.buttonLoginWith);
        btnSignUp = findViewById(R.id.buttonSignUp);

        btnLogin.setOnClickListener(v -> {
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

            loginUsuario(email, pass);
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Redirección automática si la sesión ya está iniciada
        if (!FirebaseManager.getCurrentUserUid().isEmpty()) {
            redireccionarSegunRol();
        }
    }

    private void loginUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivityLogin.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                        redireccionarSegunRol();
                    } else {
                        String errorMsg = (task.getException() != null)
                                ? "Datos incorrectos o usuario no registrado"
                                : "Error de autenticación";

                        Toast.makeText(ActivityLogin.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redireccionarSegunRol() {
        String miUid = FirebaseManager.getCurrentUserUid();

        FirebaseManager.obtenerUsuarioPorUid(miUid).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String rol = doc.getString("rol");
                Intent intent;

                // Enrutamiento según el rol del usuario
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
            // Fallback de seguridad en caso de fallo de red
            Intent intent = new Intent(ActivityLogin.this, ActivityOption.class);
            startActivity(intent);
            finish();
        });
    }
}