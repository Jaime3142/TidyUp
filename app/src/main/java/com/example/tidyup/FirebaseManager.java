package com.example.tidyup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Método para guardar tareas
    public static void guardarTarea(String titulo, String usuarioAsignado, String desc, String fecha, String puntos, OnCompleteListener<DocumentReference> listener) {
        String correo = mAuth.getCurrentUser().getEmail();

        db.collection("Usuarios").document(correo).get().addOnSuccessListener(documentSnapshot -> {
            String idGrupo = documentSnapshot.getString("id_grupo");

            Map<String, Object> tarea = new HashMap<>();
            tarea.put("titulo", titulo);
            tarea.put("usuarioAsignado", usuarioAsignado);
            tarea.put("descripcion", desc);
            tarea.put("fechaLimite", fecha);
            tarea.put("puntos", puntos); // Guardamos los puntos
            tarea.put("id_grupo", idGrupo);
            tarea.put("estado", "pendiente");
            tarea.put("asignada", correo);

            db.collection("Tareas").add(tarea).addOnCompleteListener(listener);
        });
    }

    // Método para cargar tareas filtradas por el correo del usuario asignado
    public static void cargarTareasEnContenedor(LinearLayout contenedor, LayoutInflater inflater, String correoUsuario, OnCompleteListener<QuerySnapshot> listener) {

        // 1. Consultamos las tareas filtradas por el correo del usuario asignado
        db.collection("Tareas")
                .whereEqualTo("asignada", correoUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Limpiamos el contenedor para evitar duplicados
                        contenedor.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extraemos los datos del documento
                            String idTarea = document.getId(); // El ID único para poder borrarla
                            String titulo = document.getString("titulo");
                            String usuario = document.getString("usuarioAsignado");

                            // Extraemos puntos (manejando si es String o Number en Firebase)
                            Object ptsObj = document.get("puntos");
                            String puntos = (ptsObj != null) ? ptsObj.toString() : "0";

                            // 2. Inflamos el diseño de la tarjeta (item_tarea.xml)
                            View fila = inflater.inflate(R.layout.item_tarea, null);

                            // Vinculamos los elementos del XML
                            TextView tvNombre = fila.findViewById(R.id.txtNombre);
                            TextView tvTitulo = fila.findViewById(R.id.txtTitulo);
                            TextView tvPuntos = fila.findViewById(R.id.txtPuntos);
                            CheckBox chk = fila.findViewById(R.id.checkTarea);

                            // Seteamos los textos en la tarjeta
                            if (tvNombre != null) tvNombre.setText(usuario);
                            if (tvTitulo != null) tvTitulo.setText(titulo);
                            if (tvPuntos != null) tvPuntos.setText(puntos + " pts");

                            // 3. Lógica de Borrado al marcar el CheckBox
                            if (chk != null) {
                                chk.setOnClickListener(v -> {
                                    // Llamamos al borrado en Firebase usando el ID del documento
                                    db.collection("Tareas").document(idTarea).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                // Si se borra en la nube, la quitamos de la pantalla
                                                contenedor.removeView(fila);
                                                android.widget.Toast.makeText(fila.getContext(),
                                                        "¡Tarea completada!", android.widget.Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Por si falla la conexión, desmarcamos el check
                                                chk.setChecked(false);
                                                android.widget.Toast.makeText(fila.getContext(),
                                                        "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }

                            // Añadimos la tarjeta terminada al LinearLayout de la pantalla
                            contenedor.addView(fila);
                        }
                    }
                    // Avisamos al Fragment de que el proceso ha terminado
                    listener.onComplete(task);
                });
    }

    public static void eliminarTarea(String idDocumento, OnCompleteListener<Void> listener) {
        db.collection("Tareas").document(idDocumento)
                .delete()
                .addOnCompleteListener(listener);
    }
}