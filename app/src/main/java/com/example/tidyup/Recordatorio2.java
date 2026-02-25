package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class Recordatorio2 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Recordatorio2() {
        // Constructor vacío obligatorio
    }

    public static Recordatorio2 newInstance(String param1, String param2) {
        Recordatorio2 fragment = new Recordatorio2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflar el layout para este fragmento
        View root = inflater.inflate(R.layout.fragment_recordatorio2, container, false);

        // 2. Referenciar el botón (asegúrate de que el ID sea button4 en tu XML)
        Button btnNuevoRecordatorio = root.findViewById(R.id.button4);

        // 3. Configurar el evento de clic
        btnNuevoRecordatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AQUÍ DEFINES A QUÉ FRAGMENTO QUIERES IR
                // Cambia 'FragmentUltRecordatorio' por el nombre real de tu clase de creación
                Fragment siguienteFragmento = new UltRecordatorio();

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                // Reemplaza 'fragment_container' por el ID del contenedor en tu activity_main.xml
                transaction.replace(R.id.fragmentContainerView, siguienteFragmento);

                // Añadir a la pila para poder volver atrás con el botón físico del móvil
                transaction.addToBackStack(null);

                transaction.commit();
            }
        });

        return root;
    }
}