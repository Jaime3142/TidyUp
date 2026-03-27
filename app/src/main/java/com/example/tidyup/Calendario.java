package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// IMPORT DE FIREBASE AUTH
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Calendario extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Carga tu diseño original calendario.xml
        View view = inflater.inflate(R.layout.calendario, container, false);

        // Buscamos el título por su ID original (tvTitle)
        TextView tvTitulo = view.findViewById(R.id.tvTitle);

        //  LEER EL USUARIO DE FIREBASE
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Cogemos su correo
            String emailStr = user.getEmail();
            if (emailStr != null) {
                // Reemplazamos "Mi Calendario" por el correo
                tvTitulo.setText(emailStr);
            }
        } else {
            // Por si acaso no hay nadie logueado en las pruebas
            tvTitulo.setText("Invitado");
        }

        return view;
    }
}