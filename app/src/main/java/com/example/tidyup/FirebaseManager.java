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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Autenticación y usuarios
    public static String getCurrentUserUid() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return "";
    }
    public static Task<AuthResult> registrarUsuarioAuth(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }
    public static Task<Void> crearPerfilUsuario(String uid, String nombre, String email) {
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("email", email);
        datosUsuario.put("rol", "adulto");
        datosUsuario.put("puntos", 0);

        return db.collection("Usuarios").document(uid).set(datosUsuario);
    }
    public static Task<Void> actualizarRolUsuario(String nuevoRol) {
        String uid = getCurrentUserUid();
        return db.collection("Usuarios").document(uid).update("rol", nuevoRol);
    }

    // Gestión de grupos

    public static Task<Void> crearGrupo(String nombreGrupo, String codigoAcceso, List<String> miembrosUids) {
        String miUid = getCurrentUserUid();

        if (!miembrosUids.contains(miUid)) {
            miembrosUids.add(miUid);
        }

        String idNuevoGrupo = db.collection("Grupos").document().getId();

        Map<String, Object> datosGrupo = new HashMap<>();
        datosGrupo.put("admin_id", miUid);
        datosGrupo.put("codigoAcceso", codigoAcceso);
        datosGrupo.put("nombre", nombreGrupo);
        datosGrupo.put("miembros", miembrosUids);

        return db.collection("Grupos").document(idNuevoGrupo).set(datosGrupo);
    }
    public static Task<Void> anadirMiembrosAGrupo(String idGrupo, List<String> nuevosUids) {
        return db.collection("Grupos").document(idGrupo)
                .update("miembros", FieldValue.arrayUnion(nuevosUids.toArray()));
    }

    public static Task<QuerySnapshot> obtenerMisGrupos() {
        String miUid = getCurrentUserUid();
        return db.collection("Grupos").whereArrayContains("miembros", miUid).get();
    }

    public static Task<QuerySnapshot> obtenerTodosLosUsuarios() {
        return db.collection("Usuarios").get();
    }

    public static Task<DocumentSnapshot> obtenerUsuarioPorUid(String uid) {
        return db.collection("Usuarios").document(uid).get();
    }

    public static Task<Void> eliminarMiembroDeGrupo(String idGrupo, String uidMiembro) {
        // arrayRemove busca ese UID específico en la lista y lo borra sin tocar a los demás
        return db.collection("Grupos").document(idGrupo)
                .update("miembros", FieldValue.arrayRemove(uidMiembro));
    }

    public static Task<Void> eliminarGrupo(String idGrupo) {
        return db.collection("Grupos").document(idGrupo).delete();
    }

    public static void cerrarSesion() {
        mAuth.signOut();
    }
}

// Tareas Mayores
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