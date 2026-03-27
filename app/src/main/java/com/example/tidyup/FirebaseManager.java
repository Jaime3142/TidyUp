package com.example.tidyup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // CREAR GRUPO
    public static void crearGrupo(String nombreGrupo, OnCompleteListener<Void> listener) {

        if (mAuth.getCurrentUser() == null) {
            if (listener != null) listener.onComplete(null);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> grupo = new HashMap<>();
        grupo.put("nombre", nombreGrupo);
        grupo.put("admin", uid);

        db.collection("grupos").add(grupo).addOnSuccessListener(docRef -> {

            String idGrupo = docRef.getId();

            db.collection("usuarios").document(uid)
                    .set(Collections.singletonMap("id_grupo", idGrupo), SetOptions.merge())
                    .addOnCompleteListener(listener);
        });
    }

    // CREAR TAREA
    public static void crearTarea(String titulo, String descripcion, String fecha, OnCompleteListener<Void> listener) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (listener != null) listener.onComplete(Tasks.forException(new Exception("Usuario no logueado")));
            return;
        }

        String correo = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Map<String, Object> nuevaTarea = new HashMap<>();
        nuevaTarea.put("titulo", titulo);
        nuevaTarea.put("descripcion", descripcion);
        nuevaTarea.put("fecha", fecha);
        nuevaTarea.put("estado", "pendiente");
        nuevaTarea.put("asignada", correo);

        db.collection("Tareas")
                .add(nuevaTarea)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forResult(null));
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
    }

    //  CARGAR TAREAS EN EL LINEARLAYOUT
    public static void cargarTareasEnContenedor(LinearLayout contenedor,
                                                LayoutInflater inflater,
                                                String correoUsuario,
                                                OnCompleteListener<QuerySnapshot> listener) {

        db.collection("Tareas")
                .whereEqualTo("asignada", correoUsuario)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        contenedor.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String idTarea = document.getId();
                            String titulo = document.getString("titulo");
                            String fecha = document.getString("fecha");

                            View fila = inflater.inflate(R.layout.tarea, null);

                            TextView tvNombre = fila.findViewById(R.id.txtNombre);
                            TextView tvFecha = fila.findViewById(R.id.txtTarea);
                            CheckBox chk = fila.findViewById(R.id.checkBoxT);

                            if (tvNombre != null) tvNombre.setText(titulo != null ? titulo : "");
                            if (tvFecha != null) tvFecha.setText(fecha != null ? fecha : "");


                            if (chk != null) {
                                chk.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    if (isChecked) {

                                        db.collection("Tareas").document(idTarea).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    contenedor.removeView(fila);
                                                    Toast.makeText(fila.getContext(),
                                                            "¡Tarea completada!",
                                                            Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    chk.setChecked(false);
                                                    Toast.makeText(fila.getContext(),
                                                            "Error al borrar",
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                            }

                            contenedor.addView(fila);
                        }
                    }

                    if (listener != null) {
                        listener.onComplete(task);
                    }
                });
    }
}