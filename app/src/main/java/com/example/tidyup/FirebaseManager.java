package com.example.tidyup;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue; // ¡Nueva importación vital!
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // -- 1. AUTH & USUARIOS --

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

    // -- 2. GRUPOS --

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

    // NUEVO: Método para añadir nuevos UIDs al array de miembros de un grupo existente
    public static Task<Void> anadirMiembrosAGrupo(String idGrupo, List<String> nuevosUids) {
        // arrayUnion mete los nuevos elementos sin borrar los que ya estaban
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

    // NUEVO: Método para eliminar un grupo por completo de la base de datos
    public static Task<Void> eliminarGrupo(String idGrupo) {
        return db.collection("Grupos").document(idGrupo).delete();
    }
}

