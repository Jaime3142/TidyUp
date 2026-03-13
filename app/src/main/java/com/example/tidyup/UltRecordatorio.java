package com.example.tidyup;

import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UltRecordatorio extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText etNombre;
    private EditText etFecha;
    private EditText etDescripcion;
    private Button btnCrear;

    // 🔹 Firestore
    private FirebaseFirestore db;

    private static final String TAG = "UltRecordatorio";

    public UltRecordatorio() {
        // Constructor vacío obligatorio
    }

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

        db = FirebaseFirestore.getInstance(); // 🔹 iniciar Firestore

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_ult_recordatorio, container, false);

        //  Referencias a los views
        ImageButton btnAtras = root.findViewById(R.id.imageButton6);
        etNombre = root.findViewById(R.id.nombre);
        etFecha = root.findViewById(R.id.fecha);
        etDescripcion = root.findViewById(R.id.descripcion);
        btnCrear = root.findViewById(R.id.crear);

        // 🔹 Validación de views
        if (etNombre == null || etFecha == null || etDescripcion == null || btnCrear == null || btnAtras == null) {
            Log.e(TAG, "Algún view es null. Revisa los IDs en tu XML.");
            return root;
        }

        // 🔹 Botón atrás
        btnAtras.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        // 🔹 Botón crear tarea
        btnCrear.setOnClickListener(v -> {

            String tarea = etNombre.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (tarea.isEmpty() || fecha.isEmpty() || descripcion.isEmpty()) {
                Log.w(TAG, "Algún campo está vacío, no se guardará.");
                return;
            }

            // Guardar en listas locales
            Recordatorio2.tareas.add(tarea);
            Recordatorio2.fechas.add(fecha);

            // Crear objeto para Firestore
            Map<String, Object> nuevaTarea = new HashMap<>();
            nuevaTarea.put("titulo", tarea);
            nuevaTarea.put("descripcion", descripcion);
            nuevaTarea.put("fecha_limite", fecha);
            nuevaTarea.put("estado", "pendiente");
            nuevaTarea.put("puntos", 100);
            nuevaTarea.put("asignada", "");
            nuevaTarea.put("id_grupo", "VR5NhwotrNRvItHypTqs");

            // Guardar en Firestore
            db.collection("Tareas")
                    .add(nuevaTarea)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Tarea guardada con ID: " + documentReference.getId());
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error guardando en Firestore: ", e);
                    });

        });

        return root;
    }
}