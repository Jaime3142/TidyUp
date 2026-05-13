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

// Esta clase se encarga de toda la comunicacion con Firebase.
// Desde aqui se gestionan los usuarios, grupos, tareas y notificaciones de la app.
public class FirebaseManager {

    // Conexion a la base de datos y al sistema de login de Firebase
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // ── LOGIN Y USUARIOS ─────────────────────────────────────────────────

    // Devuelve el ID del usuario que tiene la sesion abierta ahora mismo.
    // Si no hay nadie logueado devuelve una cadena vacia.
    public static String getCurrentUserUid() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return "";
    }

    // Registra un usuario nuevo con email y contraseña en Firebase.
    public static Task<AuthResult> registrarUsuarioAuth(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    // Guarda los datos basicos del usuario recien registrado en la base de datos.
    // Los puntos empiezan en 0 y el rol se asigna despues.
    public static Task<Void> crearPerfilUsuario(String uid, String nombre, String email) {
        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("nombre", nombre);
        datosUsuario.put("email", email);
        datosUsuario.put("rol", "");
        datosUsuario.put("puntos", 0);

        return db.collection("Usuarios").document(uid).set(datosUsuario);
    }

    // Cambia el rol del usuario logueado, por ejemplo de vacio a "adulto" o "adolescente".
    public static Task<Void> actualizarRolUsuario(String nuevoRol) {
        String uid = getCurrentUserUid();
        return db.collection("Usuarios").document(uid).update("rol", nuevoRol);
    }

    // ── GRUPOS ───────────────────────────────────────────────────────────

    // Crea un grupo nuevo con el nombre y codigo que se le pase.
    // Si el creador no esta ya en la lista de miembros, se añade automaticamente.
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

    // Añade nuevos miembros a un grupo que ya existe.
    // Si alguno ya estaba en el grupo no se duplica.
    public static Task<Void> anadirMiembrosAGrupo(String idGrupo, List<String> nuevosUids) {
        return db.collection("Grupos").document(idGrupo)
                .update("miembros", FieldValue.arrayUnion(nuevosUids.toArray()));
    }

    // Devuelve todos los grupos en los que esta el usuario logueado.
    public static Task<QuerySnapshot> obtenerMisGrupos() {
        String miUid = getCurrentUserUid();
        return db.collection("Grupos").whereArrayContains("miembros", miUid).get();
    }

    // Devuelve todos los usuarios registrados en la app.
    public static Task<QuerySnapshot> obtenerTodosLosUsuarios() {
        return db.collection("Usuarios").get();
    }

    // Interfaz para avisar cuando la lista de usuarios disponibles esta lista.
    public interface UsuariosDisponiblesCallback {
        void onUsuariosCargados(List<Map<String, String>> usuariosDisponibles);
        void onError(Exception e);
    }

    // Busca usuarios que todavia no pertenecen a ningun grupo.
    // Primero mira quien ya esta en algun grupo y luego filtra el resto.
    // El usuario logueado nunca aparece en la lista.
    public static void obtenerUsuariosLibres(UsuariosDisponiblesCallback callback) {
        String miUid = getCurrentUserUid();

        db.collection("Grupos").get().addOnSuccessListener(gruposSnap -> {
            // Recoge los IDs de todos los que ya tienen grupo
            List<String> uidsOcupados = new ArrayList<>();
            for (DocumentSnapshot grupo : gruposSnap.getDocuments()) {
                List<String> miembros = (List<String>) grupo.get("miembros");
                if (miembros != null) {
                    uidsOcupados.addAll(miembros);
                }
            }

            // De todos los usuarios, se queda solo con los que no tienen grupo
            db.collection("Usuarios").get().addOnSuccessListener(usuariosSnap -> {
                List<Map<String, String>> usuariosDisponibles = new ArrayList<>();

                for (DocumentSnapshot document : usuariosSnap.getDocuments()) {
                    String uid = document.getId();
                    String nombre = document.getString("nombre");
                    String email = document.getString("email");

                    if (nombre != null && email != null && !uidsOcupados.contains(uid) && !uid.equals(miUid)) {
                        Map<String, String> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("nombre", nombre);
                        userData.put("email", email);
                        // Texto que se muestra en el buscador al añadir miembros
                        userData.put("display_text", nombre + " (" + email + ")");
                        usuariosDisponibles.add(userData);
                    }
                }
                callback.onUsuariosCargados(usuariosDisponibles);
            }).addOnFailureListener(callback::onError);

        }).addOnFailureListener(callback::onError);
    }

    // Busca un usuario concreto en la base de datos usando su ID.
    public static Task<DocumentSnapshot> obtenerUsuarioPorUid(String uid) {
        return db.collection("Usuarios").document(uid).get();
    }

    // Saca a un miembro de un grupo sin tocar al resto.
    public static Task<Void> eliminarMiembroDeGrupo(String idGrupo, String uidMiembro) {
        return db.collection("Grupos").document(idGrupo)
                .update("miembros", FieldValue.arrayRemove(uidMiembro));
    }

    // Borra un grupo entero de la base de datos.
    public static Task<Void> eliminarGrupo(String idGrupo) {
        return db.collection("Grupos").document(idGrupo).delete();
    }

    // Cierra la sesion del usuario actual.
    public static void cerrarSesion() {
        mAuth.signOut();
    }

    // ── COSAS DEL ADOLESCENTE ────────────────────────────────────────────

    // Interfaz para avisar cuando el nombre del usuario ya se ha recuperado.
    public interface OnNombreRecuperadoListener {
        void onNombreRecuperado(String nombre);
    }

    // Busca el nombre del usuario logueado en la base de datos.
    // Si no lo encuentra usa el email como nombre.
    // Si no hay sesion activa devuelve "Invitado".
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
                            listener.onNombreRecuperado((nombreReal != null && !nombreReal.isEmpty()) ? nombreReal : emailUsuario);
                            return;
                        }
                    } else {
                        listener.onNombreRecuperado(emailUsuario);
                    }
                });
    }

    // Guarda una tarea nueva en la base de datos.
    // Como el spinner muestra nombres y no emails, primero busca el email
    // del usuario seleccionado antes de guardar la tarea.
    public static void guardarTarea(String titulo, String nombreAsignado, String desc, String fecha, String puntos, OnCompleteListener<DocumentReference> listener) {

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
                    tarea.put("asignada", emailAsignado);

                    db.collection("Tareas").add(tarea).addOnCompleteListener(listener);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("TAREA", "Error buscando usuario: " + e.getMessage());
                });
    }

    // Interfaz para avisar cuando la lista de correos del grupo esta lista.
    public interface CorreosCallback {
        void onCorreosLoaded(List<String> correos);
        void onError(Exception e);
    }

    // Obtiene los correos de todos los miembros del grupo del usuario logueado.
    // Primero encuentra el grupo, luego va uno a uno buscando el email de cada miembro.
    // Solo avisa cuando tiene todos los correos recogidos.
    public void obtenerCorreosDelGrupoActual(CorreosCallback callback) {
        String miUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Grupos")
                .whereArrayContains("miembros", miUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<String> miembrosIds = (List<String>) queryDocumentSnapshots.getDocuments().get(0).get("miembros");

                        if (miembrosIds != null) {
                            List<String> listaCorreos = new ArrayList<>();

                            for (String id : miembrosIds) {
                                db.collection("Usuarios").document(id).get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                String email = userDoc.getString("email");
                                                if (email != null) listaCorreos.add(email);
                                            }

                                            // Espera a tener el correo de todos antes de continuar
                                            if (listaCorreos.size() == miembrosIds.size()) {
                                                callback.onCorreosLoaded(listaCorreos);
                                            }
                                        });
                            }
                        }
                    } else {
                        callback.onError(new Exception("No se encontro el grupo"));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    // Muestra en pantalla todas las tareas del grupo.
    // Por cada tarea crea una tarjeta con el nombre del asignado, titulo, puntos,
    // descripcion y fecha. La descripcion se puede ver pulsando la flecha.
    // Al marcar una tarea como completada se borra, se suman los puntos
    // al usuario que la tenia asignada y se le manda una notificacion.
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

                            // Los puntos pueden estar guardados como texto o como numero
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
                                tvDescripcion.setText("Descripcion: " + (descripcion != null ? descripcion : "Sin descripcion"));
                            if (tvFecha != null)
                                tvFecha.setText("Fecha limite: " + (fecha != null ? fecha : "Sin fecha"));

                            // Al pulsar la flecha se muestra u oculta la descripcion y la fecha
                            if (btnExpandir != null && panelDetalle != null) {
                                View separador = fila.findViewById(R.id.separador);
                                btnExpandir.setOnClickListener(v -> {
                                    if (panelDetalle.getVisibility() == View.GONE) {
                                        panelDetalle.setVisibility(View.VISIBLE);
                                        if (separador != null) separador.setVisibility(View.VISIBLE);
                                        btnExpandir.setText("▴");
                                    } else {
                                        panelDetalle.setVisibility(View.GONE);
                                        if (separador != null) separador.setVisibility(View.GONE);
                                        btnExpandir.setText("▾");
                                    }
                                });
                            }

                            // Busca el nombre real del usuario para mostrarlo en la tarjeta
                            // porque en la tarea solo tenemos guardado su email
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
                                    // No cambia el check hasta que el usuario confirme
                                    chk.setChecked(false);

                                    new android.app.AlertDialog.Builder(fila.getContext())
                                            .setTitle("Completar tarea")
                                            .setMessage("Quieres marcar esta tarea como completada y eliminarla?")
                                            .setPositiveButton("Si, completar", (dialog, which) -> {

                                                int puntosNumericos;
                                                try {
                                                    puntosNumericos = Integer.parseInt(puntos);
                                                } catch (NumberFormatException e) {
                                                    puntosNumericos = 0;
                                                }
                                                final int puntosFinales = puntosNumericos;

                                                // Borra la tarea de la base de datos
                                                db.collection("Tareas").document(idTarea).delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            contenedor.removeView(fila);
                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                    "Tarea completada", android.widget.Toast.LENGTH_SHORT).show();

                                                            // Busca al usuario que tenia la tarea para sumarle los puntos
                                                            if (correoAsignado != null && puntosFinales > 0) {
                                                                db.collection("Usuarios")
                                                                        .whereEqualTo("email", correoAsignado)
                                                                        .get()
                                                                        .addOnSuccessListener(userSnap -> {
                                                                            if (!userSnap.isEmpty()) {
                                                                                String userDocId = userSnap.getDocuments().get(0).getId();

                                                                                // Suma los puntos al usuario asignado
                                                                                db.collection("Usuarios").document(userDocId)
                                                                                        .update("puntos", FieldValue.increment(puntosFinales))
                                                                                        .addOnSuccessListener(unused -> {
                                                                                            android.widget.Toast.makeText(fila.getContext(),
                                                                                                    "+" + puntosFinales + " puntos para " + correoAsignado,
                                                                                                    android.widget.Toast.LENGTH_SHORT).show();

                                                                                            // Guarda la notificacion en la base de datos
                                                                                            // para que aparezca en la pantalla de notificaciones
                                                                                            FirebaseManager.guardarNotificacion(
                                                                                                    userDocId, titulo, puntosFinales);

                                                                                            // Lanza la notificacion en el movil del usuario
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

    // Interfaz para avisar cuando la lista de nombres esta lista.
    public interface UsuariosCallback {
        void onUsuariosLoaded(List<String> nombres);
        void onError(Exception e);
    }

    // Busca el nombre de varios usuarios a partir de sus IDs.
    // Espera a tener todos los nombres antes de avisar.
    private void obtenerNombresDeUsuarios(List<String> ids, UsuariosCallback callback) {
        List<String> listaNombres = new ArrayList<>();
        final int total = ids.size();

        for (String id : ids) {
            db.collection("Usuarios").document(id).get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            listaNombres.add(userDoc.getString("nombre"));
                        }
                        if (listaNombres.size() == total) {
                            callback.onUsuariosLoaded(listaNombres);
                        }
                    });
        }
    }

    // Encuentra el grupo del usuario logueado y devuelve
    // los nombres de todos sus compañeros de grupo.
    public void detectarGrupoYListarUsuarios(UsuariosCallback callback) {
        String miUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("Grupos")
                .whereArrayContains("miembros", miUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot grupoDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<String> miembrosIds = (List<String>) grupoDoc.get("miembros");

                        if (miembrosIds != null) {
                            obtenerNombresDeUsuarios(miembrosIds, callback);
                        }
                    } else {
                        callback.onError(new Exception("No perteneces a ningun grupo"));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    // ── TAREAS DEL ADULTO ────────────────────────────────────────────────

    // Crea una tarea para el adulto logueado.
    // A diferencia de las tareas del grupo, estas no tienen puntos
    // ni se pueden asignar a otros miembros.
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

    // Muestra en pantalla las tareas del adulto logueado.
    // Cada tarea tiene un checkbox que al marcarlo la borra directamente
    // sin pedir confirmacion, a diferencia de las tareas del grupo.
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
                                        // Borra la tarea al marcarla
                                        db.collection("Tareas").document(idTarea).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    contenedor.removeView(fila);
                                                    Toast.makeText(fila.getContext(),
                                                            "Tarea completada",
                                                            Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Si hay un error vuelve a desmarcar el checkbox
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

    // ── NOTIFICACIONES ───────────────────────────────────────────────────

    // Guarda una notificacion en la base de datos para un usuario concreto.
    // Usa el ID del usuario para que solo el pueda verla en su pantalla.
    public static void guardarNotificacion(String uidUsuario, String tituloTarea, int puntos) {
        android.util.Log.d("NOTIF", "Intentando guardar notificacion...");
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
                        android.util.Log.d("NOTIF", "Notificacion guardada con ID: " + ref.getId()))
                .addOnFailureListener(e ->
                        android.util.Log.e("NOTIF", "Error al guardar: " + e.getMessage()));
    }

    // Escucha en tiempo real las notificaciones del usuario logueado.
    // Cada vez que se añade o borra una notificacion avisa automaticamente
    // para que la pantalla se actualice sola.
    // Hay que cancelar este listener cuando el usuario sale de la pantalla.
    public static ListenerRegistration escucharNotificaciones(EventListener<QuerySnapshot> listener) {
        String uid = getCurrentUserUid();
        return db.collection("Notificaciones")
                .whereEqualTo("uid", uid)
                .addSnapshotListener(listener);
    }

    // Borra una notificacion de la base de datos por su ID.
    public static Task<Void> eliminarNotificacion(String idNotificacion) {
        return db.collection("Notificaciones").document(idNotificacion).delete();
    }
}