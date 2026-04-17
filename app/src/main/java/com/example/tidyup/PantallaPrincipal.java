package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class PantallaPrincipal extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. Aquí conectamos este código Java con el diseño XML que creaste en el paso anterior
        View view = inflater.inflate(R.layout.fragment_pantalla_principal_adultos, container, false);

        // 2. Buscamos tus botones gigantes por su ID
        CardView cardCalendar = view.findViewById(R.id.cardCalendar);
        CardView cardEfficiency = view.findViewById(R.id.cardEfficiency);

        // 3. Le decimos qué hacer cuando tocas el botón de Calendario
        cardCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrimos el Fragment del calendario en el hueco de la MainActivity
                requireActivity().getSupportFragmentManager().beginTransaction()
                        // IMPORTANTE: Cambia "Fragment_calendario" por el nombre real de tu clase Java del calendario
                        .replace(R.id.contenedor_fragments, new Calendario())
                        .addToBackStack(null) // Para poder volver atrás con la flecha del móvil
                        .commit();
            }
        });

        // 4. (Opcional) Lo mismo para el botón del Gráfico
        cardEfficiency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí pondrías el código para abrir tu fragment del gráfico
            }
        });

        return view;
    }
}