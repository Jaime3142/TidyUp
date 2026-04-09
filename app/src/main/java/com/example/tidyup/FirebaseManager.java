package com.example.tidyup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public interface CorreosCallback {
        void onCorreosLoaded(List<String> correos);
        void onError(Exception e); // Añadimos error por seguridad
    }
    // Método para cargar tareas filtradas por el correo del usuario asignado
    public void obtenerCorreosDelGrupoActual(CorreosCallback callback) {
        String miUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Buscamos el grupo donde el usuario actual es miembro
        db.collection("Grupos")
                .whereArrayContains("miembros", miUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtenemos la lista de IDs de los miembros del primer grupo encontrado
                        List<String> miembrosIds = (List<String>) queryDocumentSnapshots.getDocuments().get(0).get("miembros");

                        if (miembrosIds != null) {
                            List<String> listaCorreos = new ArrayList<>();

                            for (String id : miembrosIds) {
                                // Buscamos el email de cada miembro en la colección Usuarios
                                db.collection("Usuarios").document(id).get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String email = userDoc.getString("email");
                                                if (email != null) listaCorreos.add(email);
                                            }

                                            // Cuando hayamos terminado con todos los IDs, devolvemos la lista
                                            if (listaCorreos.size() == miembrosIds.size()) {
                                                callback.onCorreosLoaded(listaCorreos);
                                            }
                                        });
                            }
                        }
                    } else {
                        callback.onError(new Exception("No se encontró el grupo"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }
    public static void cargarTareasDelGrupoEnContenedor(LinearLayout contenedor, LayoutInflater inflater, List<String> correosGrupo, OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Validación: Si no hay correos, limpiamos y salimos
        if (correosGrupo == null || correosGrupo.isEmpty()) {
            contenedor.removeAllViews();
            return;
        }

        // 2. Consultamos las tareas de todos los miembros del grupo
        db.collection("Tareas")
                .whereIn("asignada", correosGrupo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contenedor.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Datos de la tarea
                            String idTarea = document.getId();
                            String titulo = document.getString("titulo");
                            String correoAsignado = document.getString("asignada");

                            Object ptsObj = document.get("puntos");
                            String puntos = (ptsObj != null) ? ptsObj.toString() : "0";

                            // 3. Inflamos la vista de la tarjeta
                            View fila = inflater.inflate(R.layout.item_tarea, null);

                            TextView tvNombre = fila.findViewById(R.id.txtNombre);
                            TextView tvTitulo = fila.findViewById(R.id.txtTitulo);
                            TextView tvPuntos = fila.findViewById(R.id.txtPuntos);
                            CheckBox chk = fila.findViewById(R.id.checkTarea);

                            // Seteamos lo que ya tenemos
                            if (tvTitulo != null) tvTitulo.setText(titulo);
                            if (tvPuntos != null) tvPuntos.setText(puntos + " pts");

                            // --- LÓGICA PARA MOSTRAR NOMBRE EN VEZ DE CORREO ---
                            if (correoAsignado != null && tvNombre != null) {
                                // Buscamos al usuario que tiene ese email
                                db.collection("Usuarios")
                                        .whereEqualTo("email", correoAsignado)
                                        .get()
                                        .addOnSuccessListener(userSnap -> {
                                            if (!userSnap.isEmpty()) {
                                                // Si lo encuentra, ponemos su nombre real
                                                String nombreReal = userSnap.getDocuments().get(0).getString("nombre");
                                                tvNombre.setText(nombreReal);
                                            } else {
                                                // Si no lo encuentra, dejamos el correo para no dejarlo vacío
                                                tvNombre.setText(correoAsignado);
                                            }
                                        });
                            }

                            // 4. Lógica de borrado (Check)
                            if (chk != null) {
                                chk.setOnClickListener(v -> {
                                    db.collection("Tareas").document(idTarea).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                contenedor.removeView(fila);
                                                android.widget.Toast.makeText(fila.getContext(),
                                                        "¡Tarea completada!", android.widget.Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                chk.setChecked(false);
                                                android.widget.Toast.makeText(fila.getContext(),
                                                        "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }

                            // Añadimos la tarjeta al contenedor
                            contenedor.addView(fila);
                        }
                    }
                    listener.onComplete(task);
                });
    }

    public static void eliminarTarea(String idDocumento, OnCompleteListener<Void> listener) {
        db.collection("Tareas").document(idDocumento)
                .delete()
                .addOnCompleteListener(listener);
    }
    // interfaz de los datos de la lista de usuarios
    public interface UsuariosCallback {
        void onUsuariosLoaded(List<String> nombres);
        void onError(Exception e);
    }



    private void obtenerNombresDeUsuarios(List<String> ids, UsuariosCallback callback) {
        List<String> listaNombres = new ArrayList<>();
        final int total = ids.size();

        for (String id : ids) {
            db.collection("Usuarios").document(id).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            listaNombres.add(userDoc.getString("nombre"));
                        }
                        // Solo cuando tenemos todos los nombres, avisamos al Fragment
                        if (listaNombres.size() == total) {
                            callback.onUsuariosLoaded(listaNombres);
                        }
                    });
        }
    }
    public void detectarGrupoYListarUsuarios(UsuariosCallback callback) {
        //Obtener mi ID actual
        String miUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Buscar en la colección "Grupos" el documento donde el array "miembros" contiene mi UID
        db.collection("Grupos")
                .whereArrayContains("miembros", miUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Tomamos el primer grupo encontrado (el documento que vimos en tu captura)
                        DocumentSnapshot grupoDoc = queryDocumentSnapshots.getDocuments().get(0);

                        //Una vez tenemos el grupo, sacamos la lista de IDs de miembros
                        List<String> miembrosIds = (List<String>) grupoDoc.get("miembros");

                        if (miembrosIds != null) {
                            // Reutilizamos tu lógica de buscar nombres por ID
                            obtenerNombresDeUsuarios(miembrosIds, callback);
                        }
                    } else {
                        callback.onError(new Exception("No perteneces a ningún grupo"));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }



}