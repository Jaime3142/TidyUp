package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        EditText etUsername = findViewById(R.id.editTextTextUsername);
        EditText etPassword = findViewById(R.id.editTextTextPassword);
        Button btnLogin = findViewById(R.id.buttonLoginWith);
        Button btnSignUp = findViewById(R.id.buttonSignUp);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUsername.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(ActivityLogin.this, "No se pueden dejar campos en blanco", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityLogin.this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityLogin.this, ActivityOption.class);
                    startActivity(intent);
                }
            }
        });



        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Vamos al ActivitySignUp
                Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
                startActivity(intent);

            }
        });
    }
}