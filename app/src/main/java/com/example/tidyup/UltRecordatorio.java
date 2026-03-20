package com.example.tidyup;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UltRecordatorio extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etNombre;
    private EditText etFecha;
    private EditText etDescripcion;
    private Button btnCrear;

    private static final String TAG = "UltRecordatorio";

    public UltRecordatorio() { }

    public static UltRecordatorio newInstance(String param1, String param2) {
        UltRecordatorio fragment = new UltRecordatorio();
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

        View root = inflater.inflate(R.layout.fragment_ult_recordatorio, container, false);

        // Referencias a los views
        ImageButton btnAtras = root.findViewById(R.id.imageButton6);
        etNombre = root.findViewById(R.id.nombre);
        etFecha = root.findViewById(R.id.fecha);
        etDescripcion = root.findViewById(R.id.descripcion);
        btnCrear = root.findViewById(R.id.crear);

        // Validación
        if (etNombre == null || etFecha == null || etDescripcion == null || btnCrear == null || btnAtras == null) {
            Log.e(TAG, "Algún view es null. Revisa los IDs en tu XML.");
            return root;
        }

        // Limpiar los EditText al abrir el fragment
        etNombre.setText("");
        etFecha.setText("");
        etDescripcion.setText("");

        // Botón atrás
        btnAtras.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Botón crear tarea
        btnCrear.setOnClickListener(v -> {

            String tarea = etNombre.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (tarea.isEmpty() || fecha.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear tarea en Firebase
            FirebaseManager.crearTarea(tarea, descripcion, fecha, task -> {
                if (task != null && task.isSuccessful()) {

                    // Limpiar EditText
                    etNombre.setText("");
                    etFecha.setText("");
                    etDescripcion.setText("");

                    // Mostrar Toast
                    Toast.makeText(getContext(), "Tarea creada", Toast.LENGTH_SHORT).show();

                    // Navegar a Recordatorio2
                    reemplazarFragment(new Recordatorio2());

                } else {
                    Toast.makeText(getContext(), "Error creando tarea", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error creando tarea");
                }
            });

        });

        return root;
    }

    // Método para reemplazar fragment
    private void reemplazarFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.addToBackStack(null); // permite volver atrás
        fragmentTransaction.commit();
    }
}