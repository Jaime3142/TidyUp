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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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

    public static void guardarTarea(String titulo, String nombreAsignado, String desc, String fecha, String puntos, OnCompleteListener<DocumentReference> listener) {

        // Buscamos el email del usuario seleccionado en el spinner por su nombre
        db.collection("Usuarios")
                .whereEqualTo("nombre", nombreAsignado)
                .get()
                .addOnSuccessListener(snapAsignado -> {
                    if (snapAsignado.isEmpty()) return;

                    String emailAsignado = snapAsignado.getDocuments().get(0).getString("email");

                    Map<String, Object> tarea = new HashMap<>();
                    tarea.put("titulo", titulo);
                    tarea.put("descripcion", desc);
                    tarea.put("fechaLimite", fecha);
                    tarea.put("puntos", puntos);
                    tarea.put("estado", "pendiente");
                    tarea.put("asignada", emailAsignado); // ✅ email del usuario seleccionado

                    db.collection("Tareas").add(tarea).addOnCompleteListener(listener);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TAREA", "Error buscando usuario: " + e.getMessage());
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

        if (correosGrupo == null || correosGrupo.isEmpty()) {
            contenedor.removeAllViews();
            return;
        }

        db.collection("Tareas")
                .whereIn("asignada", correosGrupo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contenedor.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String idTarea = document.getId();
                            String titulo = document.getString("titulo");
                            String correoAsignado = document.getString("asignada");
                            String descripcion = document.getString("descripcion");
                            String fecha = document.getString("fechaLimite");

                            Object ptsObj = document.get("puntos");
                            String puntos = (ptsObj != null) ? ptsObj.toString() : "0";

                            View fila = inflater.inflate(R.layout.item_tarea, null);

                            TextView tvNombre = fila.findViewById(R.id.txtNombre);
                            TextView tvTitulo = fila.findViewById(R.id.txtTitulo);
                            TextView tvPuntos = fila.findViewById(R.id.txtPuntos);
                            CheckBox chk = fila.findViewById(R.id.checkTarea);
                            LinearLayout panelDetalle = fila.findViewById(R.id.panelDetalle);
                            TextView tvDescripcion = fila.findViewById(R.id.txtDescripcion);
                            TextView tvFecha = fila.findViewById(R.id.txtFecha);
                            TextView btnExpandir = fila.findViewById(R.id.btnExpandir);

                            if (tvTitulo != null) tvTitulo.setText(titulo);
                            if (tvPuntos != null) tvPuntos.setText(puntos + " pts");

                            if (tvDescripcion != null)
                                tvDescripcion.setText("Descripción: " + (descripcion != null ? descripcion : "Sin descripción"));
                            if (tvFecha != null)
                                tvFecha.setText("Fecha límite: " + (fecha != null ? fecha : "Sin fecha"));

                            // Lógica del desplegable
                            if (btnExpandir != null && panelDetalle != null) {
                                View separador = fila.findViewById(R.id.separador);
                                btnExpandir.setOnClickListener(v -> {
                                    if (panelDetalle.getVisibility() == View.GONE) {
                                        panelDetalle.setVisibility(View.VISIBLE);
                                        if (separador != null) separador.setVisibility(View.VISIBLE);
                                        btnExpandir.setText("▴"); // flecha arriba
                                    } else {
                                        panelDetalle.setVisibility(View.GONE);
                                        if (separador != null) separador.setVisibility(View.GONE);
                                        btnExpandir.setText("▾"); // flecha abajo
                                    }
                                });

                            }

                            // Nombre real del usuario asignado
                            if (correoAsignado != null && tvNombre != null) {
                                db.collection("Usuarios")
                                        .whereEqualTo("email", correoAsignado)
                                        .get()
                                        .addOnSuccessListener(userSnap -> {
                                            if (!userSnap.isEmpty()) {
                                                String nombreReal = userSnap.getDocuments().get(0).getString("nombre");
                                                tvNombre.setText(nombreReal);
                                            } else {
                                                tvNombre.setText(correoAsignado);
                                            }
                                        });
                            }

                            if (chk != null) {
                                chk.setOnClickListener(v -> {
                                    chk.setChecked(false);

                                    new android.app.AlertDialog.Builder(fila.getContext())
                                            .setTitle("Completar tarea")
                                            .setMessage("¿Quieres marcar esta tarea como completada y eliminarla?")
                                            .setPositiveButton("Sí, completar", (dialog, which) -> {

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

                                                            // Buscamos al usuario asignado por email
                                                            if (correoAsignado != null && puntosFinales > 0) {
                                                                db.collection("Usuarios")
                                                                        .whereEqualTo("email", correoAsignado)
                                                                        .get()
                                                                        .addOnSuccessListener(userSnap -> {
                                                                            if (!userSnap.isEmpty()) {
                                                                                String userDocId = userSnap.getDocuments().get(0).getId();

                                                                                //Sumamos puntos
                                                                                db.collection("Usuarios").document(userDocId)
                                                                                        .update("puntos", FieldValue.increment(puntosFinales))
                                                                                        .addOnSuccessListener(unused -> {
                                                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                                                    "+" + puntosFinales + " puntos para " + correoAsignado,
                                                                                                    android.widget.Toast.LENGTH_SHORT).show();

                                                                                            //Guardamos notificación con el UID del asignado
                                                                                            FirebaseManager.guardarNotificacion(
                                                                                                    userDocId, titulo, puntosFinales);

                                                                                            //Notificación en el dispositivo
                                                                                            NotificationHelper.mostrarNotificacion(
                                                                                                    fila.getContext(), titulo, puntosFinales);
                                                                                        });
                                                                            }
                                                                        });
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                    "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                                                        });
                                            })
                                            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                                            .setCancelable(true)
                                            .show();
                                });
                            }

                            contenedor.addView(fila);
                        }
                    }
                    listener.onComplete(task);
                });
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

    // ── NOTIFICACIONES ──────────────────────────────────────────────

    public static void guardarNotificacion(String uidUsuario, String tituloTarea, int puntos) {
        // LOG para verificar que se llama
        android.util.Log.d("NOTIF", "Intentando guardar notificación...");
        android.util.Log.d("NOTIF", "UID: " + uidUsuario + " | Tarea: " + tituloTarea + " | Puntos: " + puntos);

        Map<String, Object> notif = new HashMap<>();
        notif.put("uid",     uidUsuario);
        notif.put("mensaje", "Has completado \"" + tituloTarea + "\" y has ganado " + puntos + " puntos");
        notif.put("titulo",  tituloTarea);
        notif.put("puntos",  puntos);
        notif.put("fecha",   FieldValue.serverTimestamp());
        notif.put("leida",   false);

        db.collection("Notificaciones").add(notif)
                .addOnSuccessListener(ref ->
                        android.util.Log.d("NOTIF", "✅ Notificación guardada con ID: " + ref.getId()))
                .addOnFailureListener(e ->
                        android.util.Log.e("NOTIF", "❌ Error al guardar: " + e.getMessage()));
    }


    public static ListenerRegistration escucharNotificaciones(EventListener<QuerySnapshot> listener) {
        String uid = getCurrentUserUid();
        return db.collection("Notificaciones")
                .whereEqualTo("uid", uid)
                .addSnapshotListener(listener); // escucha cambios en tiempo real
    }

    public static Task<Void> eliminarNotificacion(String idNotificacion) {
        return db.collection("Notificaciones").document(idNotificacion).delete();
    }

}