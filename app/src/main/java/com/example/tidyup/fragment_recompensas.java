package com.example.tidyup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class fragment_recompensas extends Fragment {

    private TextView tvPuntosUsuario;
    private LinearLayout contenedorRecompensas;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String uid;
    private String miEmail;
    private long puntosActuales = 0;

    static class Recompensa {
        String nombre;
        String descripcion;
        int coste;
        Recompensa(String nombre, String descripcion, int coste) {
            this.nombre      = nombre;
            this.descripcion = descripcion;
            this.coste       = coste;
        }
    }

    public fragment_recompensas() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recompensas, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvPuntosUsuario       = view.findViewById(R.id.tvPuntosUsuario);
        contenedorRecompensas = view.findViewById(R.id.contenedorRecompensas);

        // LOGS
        android.util.Log.d("RECOMP", "tvPuntosUsuario null: " + (tvPuntosUsuario == null));
        android.util.Log.d("RECOMP", "contenedorRecompensas null: " + (contenedorRecompensas == null));

        uid     = FirebaseAuth.getInstance().getCurrentUser().getUid();
        miEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        android.util.Log.d("RECOMP", "UID: " + uid);
        android.util.Log.d("RECOMP", "Email: " + miEmail);

        cargarPuntosYRecompensas();
    }

    private void cargarPuntosYRecompensas() {
        db.collection("Usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    Object pts = doc.get("puntos");
                    puntosActuales = pts != null ? ((Number) pts).longValue() : 0;
                    tvPuntosUsuario.setText("Tus TidyPoints: " + puntosActuales);

                    List<Recompensa> lista = new ArrayList<>();
                    lista.add(new Recompensa(
                            "Transferir mi tarea",
                            "Pasa una de tus tareas a otro miembro del grupo",
                            40));
                    lista.add(new Recompensa(
                            "Robar tarea ajena",
                            "Te quedas con una tarea de otro y ganas sus puntos",
                            50));
                    lista.add(new Recompensa(
                            "Reducir puntos de tarea",
                            "Resta 10 puntos al valor de una tarea de otro miembro",
                            35));
                    lista.add(new Recompensa(
                            "Duplicar puntos de mi tarea",
                            "Dobla el valor de una de tus tareas pendientes",
                            45));
                    lista.add(new Recompensa(
                            "Eliminar tarea ajena",
                            "Borra una tarea pendiente de otro miembro",
                            60));

                    pintarRecompensas(lista);
                });
    }

    private void pintarRecompensas(List<Recompensa> lista) {

        contenedorRecompensas.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());


        for (Recompensa r : lista) {
            View item = inflater.inflate(R.layout.item_recompensa, null);


            //margen por código
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            int margen = (int) (12 * getResources().getDisplayMetrics().density);
            params.setMargins(8, margen, 8, margen);
            item.setLayoutParams(params);

            TextView tvNombre      = item.findViewById(R.id.tvNombreRecompensa);
            TextView tvDesc        = item.findViewById(R.id.tvDescRecompensa);
            TextView tvPuntos      = item.findViewById(R.id.tvPuntosRecompensa);
            Button   btnCanjear    = item.findViewById(R.id.btnCanjear);

            tvNombre.setText(r.nombre);
            tvDesc.setText(r.descripcion);
            tvPuntos.setText(r.coste + " pts");

            if (puntosActuales < r.coste) {
                btnCanjear.setEnabled(false);
                btnCanjear.setAlpha(0.4f);
            }

            btnCanjear.setOnClickListener(v -> {
                if (puntosActuales < r.coste) {
                    Toast.makeText(getContext(), "No tienes suficientes puntos", Toast.LENGTH_SHORT).show();
                    return;
                }
                ejecutarRecompensa(r);
            });

            contenedorRecompensas.addView(item);
        }
    }

    // ── EJECUTAR CADA RECOMPENSA ─────────────────────────────────────────

    private void ejecutarRecompensa(Recompensa r) {
        switch (r.nombre) {
            case "Transferir mi tarea":        transferirMiTarea(r);       break;
            case "Robar tarea ajena":          robarTareaAjena(r);         break;
            case "Reducir puntos de tarea":    reducirPuntosTarea(r);      break;
            case "Duplicar puntos de mi tarea":duplicarMiTarea(r);         break;
            case "Eliminar tarea ajena":       eliminarTareaAjena(r);      break;
        }
    }

    // 1. TRANSFERIR MI TAREA → selecciona tu tarea y a quién dársela
    private void transferirMiTarea(Recompensa r) {
        db.collection("Tareas").whereEqualTo("asignada", miEmail).get()
                .addOnSuccessListener(tareasSnap -> {
                    if (tareasSnap.isEmpty()) {
                        Toast.makeText(getContext(), "No tienes tareas pendientes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> tareas = tareasSnap.getDocuments();
                    String[] nombresTareas = new String[tareas.size()];
                    for (int i = 0; i < tareas.size(); i++)
                        nombresTareas[i] = tareas.get(i).getString("titulo");

                    // Primero selecciona la tarea
                    new AlertDialog.Builder(requireContext())
                            .setTitle("¿Qué tarea quieres transferir?")
                            .setItems(nombresTareas, (d, indexTarea) -> {
                                DocumentSnapshot tareaElegida = tareas.get(indexTarea);

                                // Luego selecciona a quién
                                obtenerMiembrosGrupoExceptoYo(miembros -> {
                                    String[] nombres = miembros.stream()
                                            .map(m -> m[0]).toArray(String[]::new); // nombre visible

                                    new AlertDialog.Builder(requireContext())
                                            .setTitle("¿A quién le pasas la tarea?")
                                            .setItems(nombres, (d2, indexMiembro) -> {
                                                String emailDestino = miembros.get(indexMiembro)[1];
                                                db.collection("Tareas").document(tareaElegida.getId())
                                                        .update("asignada", emailDestino)
                                                        .addOnSuccessListener(x -> descontarPuntosYRefrescar(r,
                                                                "Tarea transferida a " + nombres[indexMiembro]));
                                            }).show();
                                });
                            }).show();
                });
    }

    // 2. ROBAR TAREA AJENA → la tarea pasa a tu email
    private void robarTareaAjena(Recompensa r) {
        obtenerTareasDeOtrosMiembros(tareas -> {
            if (tareas.isEmpty()) {
                Toast.makeText(getContext(), "No hay tareas de otros miembros", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] opciones = tareas.stream()
                    .map(t -> t.getString("titulo") + " (" + t.getString("asignada") + ")")
                    .toArray(String[]::new);

            new AlertDialog.Builder(requireContext())
                    .setTitle("¿Qué tarea quieres robar?")
                    .setItems(opciones, (d, i) -> {
                        db.collection("Tareas").document(tareas.get(i).getId())
                                .update("asignada", miEmail)
                                .addOnSuccessListener(x -> descontarPuntosYRefrescar(r, "¡Tarea robada! Ahora es tuya"));
                    }).show();
        });
    }

    //REDUCIR PUNTOS DE TAREA AJENA → resta 10 pts al valor de la tarea
    private void reducirPuntosTarea(Recompensa r) {
        obtenerTareasDeOtrosMiembros(tareas -> {
            if (tareas.isEmpty()) {
                Toast.makeText(getContext(), "No hay tareas de otros miembros", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] opciones = new String[tareas.size()];
            for (int i = 0; i < tareas.size(); i++) {
                DocumentSnapshot t = tareas.get(i);
                opciones[i] = t.getString("titulo") + " — " + t.get("puntos") + " pts";
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("¿A qué tarea le reduces los puntos?")
                    .setItems(opciones, (d, i) -> {
                        DocumentSnapshot tarea = tareas.get(i);

                        //Maneja tanto String como Number
                        long ptsActuales = 0;
                        Object ptsObj = tarea.get("puntos");
                        if (ptsObj instanceof Number) {
                            ptsActuales = ((Number) ptsObj).longValue();
                        } else if (ptsObj instanceof String) {
                            try { ptsActuales = Long.parseLong((String) ptsObj); }
                            catch (NumberFormatException e) { ptsActuales = 0; }
                        }

                        long nuevoPts = Math.max(0, ptsActuales - 10);

                        db.collection("Tareas").document(tarea.getId())
                                .update("puntos", String.valueOf(nuevoPts)) // guardamos como String igual que el resto
                                .addOnSuccessListener(x -> descontarPuntosYRefrescar(r,
                                        "Tarea reducida a " + nuevoPts + " puntos"));
                    }).show();
        });
    }

    // 4. DUPLICAR PUNTOS DE MI TAREA
    private void duplicarMiTarea(Recompensa r) {
        db.collection("Tareas").whereEqualTo("asignada", miEmail).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(getContext(), "No tienes tareas pendientes", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<DocumentSnapshot> tareas = snap.getDocuments();
                    String[] opciones = new String[tareas.size()];
                    for (int i = 0; i < tareas.size(); i++)
                        opciones[i] = tareas.get(i).getString("titulo")
                                + " — " + tareas.get(i).get("puntos") + " pts";

                    new AlertDialog.Builder(requireContext())
                            .setTitle("¿Qué tarea quieres duplicar?")
                            .setItems(opciones, (d, i) -> {
                                DocumentSnapshot tarea = tareas.get(i);

                                // ✅ Mismo fix
                                long pts = 0;
                                Object ptsObj = tarea.get("puntos");
                                if (ptsObj instanceof Number) {
                                    pts = ((Number) ptsObj).longValue();
                                } else if (ptsObj instanceof String) {
                                    try { pts = Long.parseLong((String) ptsObj); }
                                    catch (NumberFormatException e) { pts = 0; }
                                }

                                final long ptsFinal = pts;
                                db.collection("Tareas").document(tarea.getId())
                                        .update("puntos", String.valueOf(ptsFinal * 2))
                                        .addOnSuccessListener(x -> descontarPuntosYRefrescar(r,
                                                "¡Puntos duplicados! Ahora vale " + (ptsFinal * 2) + " pts"));
                            }).show();
                });
    }

    // 5. ELIMINAR TAREA AJENA
    private void eliminarTareaAjena(Recompensa r) {
        obtenerTareasDeOtrosMiembros(tareas -> {
            if (tareas.isEmpty()) {
                Toast.makeText(getContext(), "No hay tareas de otros miembros", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] opciones = tareas.stream()
                    .map(t -> t.getString("titulo") + " (" + t.getString("asignada") + ")")
                    .toArray(String[]::new);

            new AlertDialog.Builder(requireContext())
                    .setTitle("¿Qué tarea quieres eliminar?")
                    .setItems(opciones, (d, i) -> {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("¿Estás seguro?")
                                .setMessage("Esta acción no se puede deshacer")
                                .setPositiveButton("Eliminar", (d2, w) ->
                                        db.collection("Tareas").document(tareas.get(i).getId()).delete()
                                                .addOnSuccessListener(x -> descontarPuntosYRefrescar(r, "Tarea eliminada")))
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }).show();
        });
    }

    // ── HELPERS ──────────────────────────────────────────────────────────

    // Descuenta los puntos de la recompensa y recarga la pantalla
    private void descontarPuntosYRefrescar(Recompensa r, String mensaje) {
        db.collection("Usuarios").document(uid)
                .update("puntos", FieldValue.increment(-r.coste))
                .addOnSuccessListener(x -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                    cargarPuntosYRecompensas();
                });
    }

    // Devuelve lista de [nombre, email] de miembros del grupo excepto yo
    interface MiembrosCallback {
        void onLoaded(List<String[]> miembros);
    }

    private void obtenerMiembrosGrupoExceptoYo(MiembrosCallback callback) {
        db.collection("Grupos").whereArrayContains("miembros", uid).get()
                .addOnSuccessListener(grupos -> {
                    if (grupos.isEmpty()) return;
                    List<String> ids = (List<String>) grupos.getDocuments().get(0).get("miembros");
                    List<String[]> resultado = new ArrayList<>();
                    if (ids == null) return;
                    final int[] contador = {0};
                    for (String id : ids) {
                        if (id.equals(uid)) { contador[0]++; continue; }
                        db.collection("Usuarios").document(id).get()
                                .addOnSuccessListener(u -> {
                                    contador[0]++;
                                    resultado.add(new String[]{
                                            u.getString("nombre"), u.getString("email")});
                                    if (contador[0] == ids.size()) callback.onLoaded(resultado);
                                });
                    }
                });
    }

    // Devuelve tareas de otros miembros del grupo
    interface TareasCallback {
        void onLoaded(List<DocumentSnapshot> tareas);
    }

    private void obtenerTareasDeOtrosMiembros(TareasCallback callback) {
        obtenerMiembrosGrupoExceptoYo(miembros -> {
            if (miembros.isEmpty()) { callback.onLoaded(new ArrayList<>()); return; }
            List<String> emails = new ArrayList<>();
            for (String[] m : miembros) emails.add(m[1]);

            db.collection("Tareas").whereIn("asignada", emails).get()
                    .addOnSuccessListener(snap -> {
                        List<DocumentSnapshot> lista = new ArrayList<>(snap.getDocuments());
                        callback.onLoaded(lista);
                    });
        });
    }
}