package com.example.tidyup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedor_pruebas, new FragmentGroups())
                    .commit();
        }
    }
}