package com.example.tidyup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivitySignUp extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.editTextTextEmailAddress);
        etEmail = findViewById(R.id.editTextTextEmailAddress2);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnRegister = findViewById(R.id.buttonSignUp);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (username.isEmpty() ||email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(ActivitySignUp.this, "Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivitySignUp.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivitySignUp.this, ActivityLogin.class);
                    startActivity(intent);

                    finish();
                }
            }
        });
    }
}