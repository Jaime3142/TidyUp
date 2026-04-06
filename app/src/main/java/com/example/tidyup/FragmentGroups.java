package com.example.tidyup;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentGroups extends Fragment {

    private Button btnCrearNuevoGrupoCentral;
    private LinearLayout contenedorDinamicoGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        btnCrearNuevoGrupoCentral = view.findViewById(R.id.btnCrearNuevoGrupoCentral);
        contenedorDinamicoGrupos = view.findViewById(R.id.contenedorDinamicoGrupos);

        btnCrearNuevoGrupoCentral.setOnClickListener(v -> abrirDialogoCrearGrupo());

        cargarMisGrupos();

        return view;
    }

    private void cargarMisGrupos() {
        contenedorDinamicoGrupos.removeAllViews();

        FirebaseManager.obtenerMisGrupos().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> misGrupos = queryDocumentSnapshots.getDocuments();

            if (misGrupos.isEmpty()) {
                TextView tvVacio = new TextView(getContext());
                tvVacio.setText("Aún no perteneces a ningún grupo.\n¡Crea uno arriba!");
                tvVacio.setTextSize(16);
                tvVacio.setTextColor(Color.DKGRAY);
                tvVacio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                contenedorDinamicoGrupos.addView(tvVacio);
                return;
            }

            for (DocumentSnapshot grupoDoc : misGrupos) {
                View vistaGrupo = getLayoutInflater().inflate(R.layout.item_group, contenedorDinamicoGrupos, false);

                Button btnNombreGrupo = vistaGrupo.findViewById(R.id.btnNombreGrupo);
                Button btnAnadirMiembro = vistaGrupo.findViewById(R.id.btnAnadirMiembro);
                LinearLayout contenedorMiembros = vistaGrupo.findViewById(R.id.contenedorMiembros);

                String idGrupo = grupoDoc.getId();
                String nombreGrupo = grupoDoc.getString("nombre");
                String adminId = grupoDoc.getString("admin_id");
                String codigoAcceso = grupoDoc.getString("codigoAcceso"); // RECUPERAMOS EL CÓDIGO
                List<String> miembrosUids = (List<String>) grupoDoc.get("miembros");

                btnNombreGrupo.setText(nombreGrupo);

                // Al mantener pulsado el botón del grupo nos saldrán las opciones
                btnNombreGrupo.setOnLongClickListener(v -> {
                    mostrarOpcionesDeGrupo(idGrupo, nombreGrupo, adminId, codigoAcceso);
                    return true;
                });

                // Al pulsar el botón de +, nos pedirá un código
                btnAnadirMiembro.setOnClickListener(v -> {
                    String miUid = FirebaseManager.getCurrentUserUid();
                    if (miUid.equals(adminId)) {
                        // El admin pasa sin código
                        abrirDialogoAnadirMiembro(idGrupo, miembrosUids);
                    } else {
                        // Los demás deben poner el código
                        verificarCodigoAcceso(codigoAcceso, () -> abrirDialogoAnadirMiembro(idGrupo, miembrosUids));
                    }
                });

                // Protección para poder eliminar de forma individual
                cargarIntegrantesEnPantalla(idGrupo, miembrosUids, contenedorMiembros, adminId, codigoAcceso);

                contenedorDinamicoGrupos.addView(vistaGrupo);
            }
        }).addOnFailureListener(e -> Log.e("TIDYUP", "Error al cargar grupos", e));
    }

    // Verificación de código
    private void verificarCodigoAcceso(String codigoReal, Runnable accionPermitida) {
        final EditText inputCodigo = new EditText(getContext());
        inputCodigo.setHint("Escribe el código del grupo");

        FrameLayout layout = new FrameLayout(getContext());
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputCodigo);

        new AlertDialog.Builder(getContext())
                .setTitle("Acceso Restringido")
                .setMessage("Necesitas conocer el código secreto del grupo para hacer esto.")
                .setView(layout)
                .setPositiveButton("Verificar", (dialog, which) -> {
                    String codigoIntroducido = inputCodigo.getText().toString().trim();
                    if (codigoIntroducido.equals(codigoReal)) {
                        accionPermitida.run(); // Si acierta, ejecutamos la acción que intentaba hacer
                    } else {
                        Toast.makeText(getContext(), "Código incorrecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarOpcionesDeGrupo(String idGrupo, String nombreGrupo, String adminId, String codigoAcceso) {
        String miUid = FirebaseManager.getCurrentUserUid();

        if (miUid.equals(adminId)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Opciones de Admin")
                    .setMessage("Eres el administrador del grupo '" + nombreGrupo + "'.")
                    .setPositiveButton("Destruir Grupo", (dialog, which) -> {
                        FirebaseManager.eliminarGrupo(idGrupo).addOnSuccessListener(aVoid -> cargarMisGrupos());
                        Toast.makeText(getContext(), "Grupo eliminado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {

            new AlertDialog.Builder(getContext())
                    .setTitle("Opciones del Grupo")
                    .setMessage("¿Qué deseas hacer con '" + nombreGrupo + "'?")
                    .setPositiveButton("Abandonar Grupo", (dialog, which) -> {
                        FirebaseManager.eliminarMiembroDeGrupo(idGrupo, miUid).addOnSuccessListener(aVoid -> cargarMisGrupos());
                        Toast.makeText(getContext(), "Has salido del grupo", Toast.LENGTH_SHORT).show();
                    })
                    .setNeutralButton("Opciones Avanzadas", (dialog, which) -> {
                        verificarCodigoAcceso(codigoAcceso, () -> {
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Código Verificado")
                                    .setMessage("¿Seguro que quieres destruir este grupo para todos?")
                                    .setPositiveButton("Sí, destruir", (d, w) -> {
                                        FirebaseManager.eliminarGrupo(idGrupo).addOnSuccessListener(aVoid -> cargarMisGrupos());
                                    })
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                        });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    private void cargarIntegrantesEnPantalla(String idDelGrupo, List<String> miembrosUids, LinearLayout contenedor, String adminId, String codigoAcceso) {
        contenedor.removeAllViews();
        if (miembrosUids == null) return;

        String miUid = FirebaseManager.getCurrentUserUid();

        for (String uid : miembrosUids) {
            FirebaseManager.obtenerUsuarioPorUid(uid).addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String nombre = doc.getString("nombre");
                    String rol = doc.getString("rol");

                    TextView tvMiembro = new TextView(getContext());
                    tvMiembro.setText("👤 " + nombre + " (" + rol + ")");
                    tvMiembro.setTextSize(16);
                    tvMiembro.setTextColor(Color.BLACK);
                    tvMiembro.setTypeface(null, Typeface.BOLD);
                    tvMiembro.setPadding(20, 10, 0, 10);

                    tvMiembro.setOnClickListener(v -> {
                        if (uid.equals(miUid)) {
                            mostrarDialogoEliminar(idDelGrupo, uid, nombre, miUid);
                        } else if (miUid.equals(adminId)) {
                            mostrarDialogoEliminar(idDelGrupo, uid, nombre, miUid);
                        } else {
                            verificarCodigoAcceso(codigoAcceso, () -> mostrarDialogoEliminar(idDelGrupo, uid, nombre, miUid));
                        }
                    });

                    contenedor.addView(tvMiembro);
                }
            });
        }
    }

    private void mostrarDialogoEliminar(String idGrupo, String uidMiembro, String nombreMiembro, String miUid) {
        String mensaje = uidMiembro.equals(miUid) ?
                "¿Estás seguro de que quieres abandonar este grupo?" :
                "¿Quieres eliminar a " + nombreMiembro + " del grupo?";

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar integrante")
                .setMessage(mensaje)
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseManager.eliminarMiembroDeGrupo(idGrupo, uidMiembro)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Integrante eliminado", Toast.LENGTH_SHORT).show();
                                cargarMisGrupos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .create().show();
    }

    private void abrirDialogoAnadirMiembro(String idDelGrupoAEditar, List<String> miembrosActuales) {
        FirebaseManager.obtenerTodosLosUsuarios().addOnSuccessListener(querySnapshot -> {
            List<String> nombresUsuarios = new ArrayList<>();
            List<String> uidsUsuarios = new ArrayList<>();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                String uid = document.getId();

                if (!miembrosActuales.contains(uid)) {
                    String nombre = document.getString("nombre");
                    String email = document.getString("email");
                    nombresUsuarios.add(nombre + " (" + email + ")");
                    uidsUsuarios.add(uid);
                }
            }

            if (nombresUsuarios.isEmpty()) {
                Toast.makeText(getContext(), "Ya has añadido a todos los usuarios.", Toast.LENGTH_LONG).show();
                return;
            }

            String[] arrayNombres = nombresUsuarios.toArray(new String[0]);
            boolean[] seleccionados = new boolean[nombresUsuarios.size()];
            List<String> nuevosUidsSeleccionados = new ArrayList<>();

            new AlertDialog.Builder(getContext())
                    .setTitle("Añadir nuevos miembros")
                    .setMultiChoiceItems(arrayNombres, seleccionados, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            nuevosUidsSeleccionados.add(uidsUsuarios.get(which));
                        } else {
                            nuevosUidsSeleccionados.remove(uidsUsuarios.get(which));
                        }
                    })
                    .setPositiveButton("Añadir", (dialog, which) -> {
                        if (!nuevosUidsSeleccionados.isEmpty()) {
                            FirebaseManager.anadirMiembrosAGrupo(idDelGrupoAEditar, nuevosUidsSeleccionados)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Miembros añadidos con éxito", Toast.LENGTH_SHORT).show();
                                        cargarMisGrupos();
                                    });
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void abrirDialogoCrearGrupo() {
        FirebaseManager.obtenerTodosLosUsuarios().addOnSuccessListener(querySnapshot -> {
            List<String> nombresUsuarios = new ArrayList<>();
            List<String> uidsUsuarios = new ArrayList<>();
            String miUid = FirebaseManager.getCurrentUserUid();

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                String nombre = document.getString("nombre");
                String uid = document.getId();

                if (nombre != null && !uid.equals(miUid)) {
                    String email = document.getString("email");
                    nombresUsuarios.add(nombre + " (" + email + ")");
                    uidsUsuarios.add(uid);
                }
            }

            String[] arrayNombres = nombresUsuarios.toArray(new String[0]);
            boolean[] seleccionados = new boolean[nombresUsuarios.size()];
            List<String> uidsSeleccionados = new ArrayList<>();

            final EditText inputNombreGrupo = new EditText(getContext());
            inputNombreGrupo.setHint("Nombre del grupo (ej. Familia)");

            final EditText inputCodigoAcceso = new EditText(getContext());
            inputCodigoAcceso.setHint("Código secreto (ej. 1234)");

            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputNombreGrupo);
            layout.addView(inputCodigoAcceso);

            new AlertDialog.Builder(getContext())
                    .setTitle("Crear Nuevo Grupo")
                    .setView(layout)
                    .setMultiChoiceItems(arrayNombres, seleccionados, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            uidsSeleccionados.add(uidsUsuarios.get(which));
                        } else {
                            uidsSeleccionados.remove(uidsUsuarios.get(which));
                        }
                    })
                    .setPositiveButton("Crear", (dialog, which) -> {
                        String nombreGrupo = inputNombreGrupo.getText().toString().trim();
                        String codigoAcceso = inputCodigoAcceso.getText().toString().trim();

                        if (nombreGrupo.isEmpty() || codigoAcceso.isEmpty()) {
                            Toast.makeText(getContext(), "Rellena nombre y código", Toast.LENGTH_LONG).show();
                        } else {
                            FirebaseManager.crearGrupo(nombreGrupo, codigoAcceso, uidsSeleccionados)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "¡Grupo creado!", Toast.LENGTH_SHORT).show();
                                        cargarMisGrupos();
                                    });
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}