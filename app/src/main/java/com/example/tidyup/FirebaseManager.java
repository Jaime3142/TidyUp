package com.example.tidyup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance(); // llamamos a la base de datos de firebase
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance(); // llamamos a la auth de firebase

    public static void crearGrupo(String nombreGrupo, OnCompleteListener<Void> listener) { // mwtodo para crear grupos   (el listener avisa al activity de que firebase a acabado)
        if (mAuth.getCurrentUser() == null) {
            // Manejar error: usuario no logueado
            listener.onComplete(null); // o lanzar excepción
            return;
        }
        String uid = mAuth.getCurrentUser().getUid(); // coge el Uid del usuario

        Map<String, Object> grupo = new HashMap<>(); // mapeo de los datos
        grupo.put("nombre", nombreGrupo); // nombre del grupo
        grupo.put("admin", uid); //  usar el UID del usuario actual // Uid del usuario

        db.collection("grupos").add(grupo).addOnSuccessListener(docRef -> { // referencia a la coleccion grupos  el addsucces es para que firebase responda si el grupo se creo bien
            String idGrupo = docRef.getId(); // extrae el id creado de forma automatica

            //Vincular al usuario
            db.collection("usuarios").document(uid)
                    .set(Collections.singletonMap("id_grupo", idGrupo), SetOptions.merge())
                    .addOnCompleteListener(listener);
        });
    }

    public static void crearTarea(String titulo, String descripcion, String fecha, OnCompleteListener<Void> listener) {

        String correo = "jnc0021@alu.medac.es";

        Map<String, Object> nuevaTarea = new HashMap<>();
        nuevaTarea.put("titulo", titulo);
        nuevaTarea.put("descripcion", descripcion);
        nuevaTarea.put("fecha", fecha);
        nuevaTarea.put("estado", "pendiente");
        nuevaTarea.put("puntos", 100);
        nuevaTarea.put("asignada", correo);
      //  nuevaTarea.put("id_grupo", "VR5NhwotrNRvItHypTqs");

        db.collection("Tareas")
                .add(nuevaTarea)
                .addOnSuccessListener(documentReference -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forResult(null)); // ✔ Task exitoso
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e)); // ✔ Task con error
                    }
                });
    }
    }

