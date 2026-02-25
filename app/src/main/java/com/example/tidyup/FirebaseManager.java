package com.example.tidyup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance(); // llamamos a la base de datos de firebase
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance(); // llamamos a la auth de firebase

    public static void crearGrupo(String nombreGrupo, OnCompleteListener<Void> listener) { // mwtodo para crear grupos   (el listener avisa al activity de que firebase a acabado)
        String uid = mAuth.getCurrentUser().getUid();  // coge el Uid del usuario

        Map<String, Object> grupo = new HashMap<>(); // mapeo de los datos
        grupo.put("nombre", nombreGrupo); // nombre del grupo
        grupo.put("admin", "Xd6MCWg8GhzzCRi1JlPM"); // Uid del usuario

        db.collection("grupos").add(grupo).addOnSuccessListener(docRef -> { // referencia a la coleccion grupos  el addsucces es para que firebase responda si el grupo se creo bien
            String idGrupo = docRef.getId(); // extrae el id creado de forma automatica

            //Vincular al usuario
            db.collection("usuarios").document(uid) // pedir Uid del usuario
                    .update("id_grupo", idGrupo) // Asignar Uid del grupo al usuario
                    .addOnCompleteListener(listener);
            // Usamos el listener para avisar a la Activity que ya terminó
        });
    }
    }

