package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log; // Añadido
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
    private EditText etEmail, etPassword; // Cambiados nombres para claridad
    private Button btnLogin;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Referencias a los IDs de tu XML
        etEmail = findViewById(R.id.editTextTextUsername);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.buttonLoginWith);
        btnSignUp = findViewById(R.id.buttonSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                // Validaciones locales
                if (email.isEmpty()) {
                    etEmail.setError("Introduce tu correo");
                    return;
                }
                if (pass.isEmpty()) {
                    etPassword.setError("Introduce la contraseña");
                    return;
                }

                // Intentar iniciar sesión en Firebase
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


    private void loginUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Las credenciales coinciden
                            Toast.makeText(ActivityLogin.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                            irAHome();
                        } else {
                            // ERROR: Usuario no registrado, contraseña mal, o sin internet
                            String errorMsg = "Error de autenticación";

                            if (task.getException() != null) {
                                errorMsg = "Datos incorrectos o usuario no registrado";
                            }

                            Toast.makeText(ActivityLogin.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void irAHome() {
        Intent intent = new Intent(ActivityLogin.this, ActivityOption.class);
        startActivity(intent);
        finish(); // Evita que el usuario vuelva al Login con el botón "Atrás"
    }
}