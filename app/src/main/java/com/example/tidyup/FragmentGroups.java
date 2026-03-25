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

    private Button btnPlus1, btnPlus2;
    private Button btnGrupo1, btnGrupo2;
    private LinearLayout contenedorMiembros1, contenedorMiembros2;

    private String idGrupo1 = null;
    private String idGrupo2 = null;
    private List<String> miembrosGrupo1 = new ArrayList<>();
    private List<String> miembrosGrupo2 = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        btnGrupo1 = view.findViewById(R.id.button4);
        btnGrupo2 = view.findViewById(R.id.button12);
        btnPlus1 = view.findViewById(R.id.button11);
        btnPlus2 = view.findViewById(R.id.button13);
        contenedorMiembros1 = view.findViewById(R.id.contenedorMiembros1);
        contenedorMiembros2 = view.findViewById(R.id.contenedorMiembros2);

        cargarMisGrupos();

        btnPlus1.setOnClickListener(v -> {
            if (idGrupo1 != null) {
                abrirDialogoAnadirMiembro(idGrupo1, miembrosGrupo1);
            } else {
                abrirDialogoCrearGrupo();
            }
        });

        btnPlus2.setOnClickListener(v -> {
            if (idGrupo2 != null) {
                abrirDialogoAnadirMiembro(idGrupo2, miembrosGrupo2);
            } else {
                abrirDialogoCrearGrupo();
            }
        });

        return view;
    }

    private void cargarMisGrupos() {
        idGrupo1 = null;
        idGrupo2 = null;
        miembrosGrupo1.clear();
        miembrosGrupo2.clear();

        FirebaseManager.obtenerMisGrupos().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> misGrupos = queryDocumentSnapshots.getDocuments();

            if (misGrupos.size() > 0) {
                DocumentSnapshot grupo1 = misGrupos.get(0);
                idGrupo1 = grupo1.getId();
                miembrosGrupo1 = (List<String>) grupo1.get("miembros");

                btnGrupo1.setText(grupo1.getString("nombre"));
                // Le pasamos también el idGrupo1 para saber de dónde borrar
                cargarIntegrantesEnPantalla(idGrupo1, miembrosGrupo1, contenedorMiembros1);
            } else {
                btnGrupo1.setText("Grupo 1 (Vacío)");
                contenedorMiembros1.removeAllViews();
            }

            if (misGrupos.size() > 1) {
                DocumentSnapshot grupo2 = misGrupos.get(1);
                idGrupo2 = grupo2.getId();
                miembrosGrupo2 = (List<String>) grupo2.get("miembros");

                btnGrupo2.setText(grupo2.getString("nombre"));
                // Le pasamos también el idGrupo2 para saber de dónde borrar
                cargarIntegrantesEnPantalla(idGrupo2, miembrosGrupo2, contenedorMiembros2);
            } else {
                btnGrupo2.setText("Grupo 2 (Vacío)");
                contenedorMiembros2.removeAllViews();
            }
        }).addOnFailureListener(e -> Log.e("TIDYUP", "Error al cargar grupos", e));
    }

    // ACTUALIZADO: Recibe el ID del grupo para poder pasárselo a la función de eliminar
    private void cargarIntegrantesEnPantalla(String idDelGrupo, List<String> miembrosUids, LinearLayout contenedor) {
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

                    // NUEVO: Al tocar el nombre, mostramos la opción de eliminar
                    tvMiembro.setOnClickListener(v -> mostrarDialogoEliminar(idDelGrupo, uid, nombre, miUid));

                    contenedor.addView(tvMiembro);
                }
            });
        }
    }

    // NUEVO: Diálogo de confirmación para eliminar a alguien
    private void mostrarDialogoEliminar(String idGrupo, String uidMiembro, String nombreMiembro, String miUid) {
        // Mensaje personalizado: si tocas tu propio nombre, te pregunta si quieres salir
        String mensaje = uidMiembro.equals(miUid) ?
                "¿Estás seguro de que quieres abandonar este grupo?" :
                "¿Quieres eliminar a " + nombreMiembro + " del grupo?";

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar integrante")
                .setMessage(mensaje)
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    // Llamamos a nuestro nuevo método del FirebaseManager
                    FirebaseManager.eliminarMiembroDeGrupo(idGrupo, uidMiembro)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Integrante eliminado", Toast.LENGTH_SHORT).show();
                                cargarMisGrupos(); // Recargamos para que desaparezca de la lista
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
                Toast.makeText(getContext(), "Ya has añadido a todos los usuarios de la app.", Toast.LENGTH_LONG).show();
                return;
            }

            String[] arrayNombres = nombresUsuarios.toArray(new String[0]);
            boolean[] seleccionados = new boolean[nombresUsuarios.size()];
            List<String> nuevosUidsSeleccionados = new ArrayList<>();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Añadir nuevos miembros");

            builder.setMultiChoiceItems(arrayNombres, seleccionados, (dialog, which, isChecked) -> {
                if (isChecked) {
                    nuevosUidsSeleccionados.add(uidsUsuarios.get(which));
                } else {
                    nuevosUidsSeleccionados.remove(uidsUsuarios.get(which));
                }
            });

            builder.setPositiveButton("Añadir", (dialog, which) -> {
                if (nuevosUidsSeleccionados.isEmpty()) {
                    Toast.makeText(getContext(), "No seleccionaste a nadie", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseManager.anadirMiembrosAGrupo(idDelGrupoAEditar, nuevosUidsSeleccionados)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Miembros añadidos con éxito", Toast.LENGTH_SHORT).show();
                                cargarMisGrupos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al añadir: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.create().show();
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error al obtener usuarios", Toast.LENGTH_SHORT).show());
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
            inputCodigoAcceso.setHint("Código de acceso (ej. 1234)");

            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);
            layout.addView(inputNombreGrupo);
            layout.addView(inputCodigoAcceso);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Crear Nuevo Grupo");
            builder.setView(layout);

            builder.setMultiChoiceItems(arrayNombres, seleccionados, (dialog, which, isChecked) -> {
                if (isChecked) {
                    uidsSeleccionados.add(uidsUsuarios.get(which));
                } else {
                    uidsSeleccionados.remove(uidsUsuarios.get(which));
                }
            });

            builder.setPositiveButton("Crear", (dialog, which) -> {
                String nombreGrupo = inputNombreGrupo.getText().toString().trim();
                String codigoAcceso = inputCodigoAcceso.getText().toString().trim();

                if (nombreGrupo.isEmpty() || codigoAcceso.isEmpty()) {
                    Toast.makeText(getContext(), "Rellena nombre y código", Toast.LENGTH_LONG).show();
                } else {
                    FirebaseManager.crearGrupo(nombreGrupo, codigoAcceso, uidsSeleccionados)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "¡Grupo creado con éxito!", Toast.LENGTH_SHORT).show();
                                cargarMisGrupos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.create().show();
        });
    }
}