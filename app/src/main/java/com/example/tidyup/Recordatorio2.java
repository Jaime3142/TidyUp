package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class Recordatorio2 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    public static ArrayList<String> tareas = new ArrayList<>();
    public static ArrayList<String> fechas = new ArrayList<>();

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

        View root = inflater.inflate(R.layout.fragment_recordatorio2, container, false);

        LinearLayout contenedor = root.findViewById(R.id.Lista);

        Button btnNuevoRecordatorio = root.findViewById(R.id.button4);

        btnNuevoRecordatorio.setOnClickListener(v -> {
            Fragment siguienteFragmento = new UltRecordatorio();

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainerView, siguienteFragmento);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        //  CARGAR TAREAS
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String correoActual = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            FirebaseManager.cargarTareasEnContenedor(contenedor, inflater, correoActual, task -> {
                if (!task.isSuccessful() && getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar tareas", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return root;
    }
}