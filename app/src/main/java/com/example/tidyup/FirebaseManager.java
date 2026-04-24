package com.example.tidyup;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
// Funciones Adolescentes
    // Método para guardar tareas


    // Definimos una interfaz para avisar cuando el nombre esté listo
    public interface OnNombreRecuperadoListener {
        void onNombreRecuperado(String nombre);
    }

    public void obtenerNombreDesdeFirestore(OnNombreRecuperadoListener listener) {
        if (mAuth.getCurrentUser() == null) {
            listener.onNombreRecuperado("Invitado");
            return;
        }

        String emailUsuario = mAuth.getCurrentUser().getEmail();

        db.collection("Usuarios")
                .whereEqualTo("email", emailUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreReal = document.getString("nombre");
                            // Si el campo nombre existe, lo devolvemos; si no, el email
                            listener.onNombreRecuperado((nombreReal != null && !nombreReal.isEmpty()) ? nombreReal : emailUsuario);
                            return; // Salimos del bucle una vez encontrado
                        }
                    } else {
                        // Fallo o no existe el documento
                        listener.onNombreRecuperado(emailUsuario);
                    }
                });
    }

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
                                    // Evitamos que el check cambie visualmente hasta confirmar
                                    chk.setChecked(false);

                                    new android.app.AlertDialog.Builder(fila.getContext())
                                            .setTitle("Completar tarea")
                                            .setMessage("¿Quieres marcar esta tarea como completada y eliminarla?")
                                            .setPositiveButton("Sí, completar", (dialog, which) -> {
                                                // Convertimos los puntos a número antes de borrar la tarea
                                                int puntosNumericos;
                                                try {
                                                    puntosNumericos = Integer.parseInt(puntos);
                                                } catch (NumberFormatException e) {
                                                    puntosNumericos = 0;
                                                }
                                                final int puntosFinales = puntosNumericos;

                                                // 1. Borramos la tarea
                                                db.collection("Tareas").document(idTarea).delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            contenedor.removeView(fila);
                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                    "¡Tarea completada!", android.widget.Toast.LENGTH_SHORT).show();

                                                            // 2. Sumamos los puntos al usuario correspondiente
                                                            if (correoAsignado != null && puntosFinales > 0) {
                                                                db.collection("Usuarios")
                                                                        .whereEqualTo("email", correoAsignado)
                                                                        .get()
                                                                        .addOnSuccessListener(userSnap -> {
                                                                            if (!userSnap.isEmpty()) {
                                                                                String userDocId = userSnap.getDocuments().get(0).getId();
                                                                                db.collection("Usuarios").document(userDocId)
                                                                                        .update("puntos", com.google.firebase.firestore.FieldValue.increment(puntosFinales))
                                                                                        .addOnSuccessListener(unused ->
                                                                                                android.widget.Toast.makeText(fila.getContext(),
                                                                                                        "+" + puntosFinales + " puntos para " + correoAsignado,
                                                                                                        android.widget.Toast.LENGTH_SHORT).show()
                                                                                        );
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                    "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                                // No hacemos nada, el check ya está en false
                                                dialog.dismiss();
                                            })
                                            .setCancelable(true)
                                            .show();
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

    // Tareas Mayores
    public static void crearTarea(String titulo, String descripcion, String fecha, OnCompleteListener<Void> listener) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (listener != null)
                listener.onComplete(Tasks.forException(new Exception("Usuario no logueado")));
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

                            if (tvNombre != null)
                                tvNombre.setText(titulo != null ? titulo : "");
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