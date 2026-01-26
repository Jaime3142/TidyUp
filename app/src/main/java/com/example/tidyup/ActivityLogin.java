package com.example.tidyup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnSignUp = findViewById(R.id.buttonSignUp);
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