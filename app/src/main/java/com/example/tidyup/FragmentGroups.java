package com.example.tidyup;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FragmentGroups extends Fragment {

    private Button btnCrearNuevoGrupoCentral;
    private LinearLayout contenedorDinamicoGrupos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        btnCrearNuevoGrupoCentral = view.findViewById(R.id.btnCrearNuevoGrupoCentral);
        contenedorDinamicoGrupos = view.findViewById(R.id.contenedorDinamicoGrupos);

        // AHORA LLAMAMOS AL MÉTODO DE VERIFICACIÓN ANTES DE DEJARLE CREAR
        btnCrearNuevoGrupoCentral.setOnClickListener(v -> verificarYCrearGrupo());

        cargarMisGrupos();

        return view;
    }

    // --- NUEVO MÉTODO PARA FRENAR AL USUARIO SI YA TIENE GRUPO ---
    private void verificarYCrearGrupo() {
        FirebaseManager.obtenerMisGrupos().addOnSuccessListener(query -> {
            if (!query.isEmpty()) {
                // Si el resultado no está vacío, significa que ya estamos en un grupo
                new AlertDialog.Builder(getContext())
                        .setTitle("Acción bloqueada")
                        .setMessage("No puedes pertenecer a más de un grupo. Si deseas crear o entrar a otro, tendrás que salirte de tu grupo actual primero.")
                        .setPositiveButton("Entendido", null)
                        .show();
            } else {
                // Si estamos libres, le dejamos abrir el diálogo para crear
                abrirDialogoCrearGrupo();
            }
        });
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
                String codigoAcceso = grupoDoc.getString("codigoAcceso");
                List<String> miembrosUids = (List<String>) grupoDoc.get("miembros");

                btnNombreGrupo.setText(nombreGrupo);

                // Opciones del grupo
                btnNombreGrupo.setOnLongClickListener(v -> {
                    mostrarOpcionesDeGrupo(idGrupo, nombreGrupo, adminId, codigoAcceso);
                    return true;
                });

                // Añadir miembro protegido
                btnAnadirMiembro.setOnClickListener(v -> {
                    String miUid = FirebaseManager.getCurrentUserUid();
                    if (miUid.equals(adminId)) {
                        abrirDialogoAnadirMiembro(idGrupo, miembrosUids);
                    } else {
                        verificarCodigoAcceso(codigoAcceso, () -> abrirDialogoAnadirMiembro(idGrupo, miembrosUids));
                    }
                });

                cargarIntegrantesEnPantalla(idGrupo, miembrosUids, contenedorMiembros, adminId, codigoAcceso);
                contenedorDinamicoGrupos.addView(vistaGrupo);
            }
        }).addOnFailureListener(e -> Log.e("TIDYUP", "Error al cargar grupos", e));
    }

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
                        accionPermitida.run();
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
                        if (uid.equals(miUid) || miUid.equals(adminId)) {
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

    // --- AÑADIR MIEMBROS (VERSIÓN LIMPIA CON BUSCADOR Y SOLO USUARIOS LIBRES) ---
    private void abrirDialogoAnadirMiembro(String idDelGrupoAEditar, List<String> miembrosActuales) {

        FirebaseManager.obtenerUsuariosLibres(new FirebaseManager.UsuariosDisponiblesCallback() {
            @Override
            public void onUsuariosCargados(List<Map<String, String>> usuariosDisponibles) {
                if (usuariosDisponibles.isEmpty()) {
                    Toast.makeText(getContext(), "No hay usuarios libres disponibles para añadir.", Toast.LENGTH_LONG).show();
                    return;
                }

                // 1. Configuramos el Layout Principal
                LinearLayout layoutPrincipal = new LinearLayout(getContext());
                layoutPrincipal.setOrientation(LinearLayout.VERTICAL);
                layoutPrincipal.setPadding(50, 20, 50, 20);

                // 2. Buscador
                EditText inputBuscador = new EditText(getContext());
                inputBuscador.setHint("🔍 Buscar por correo...");
                layoutPrincipal.addView(inputBuscador);

                // 3. Contenedor de la lista
                ScrollView scrollView = new ScrollView(getContext());
                LinearLayout contenedorLista = new LinearLayout(getContext());
                contenedorLista.setOrientation(LinearLayout.VERTICAL);
                contenedorLista.setPadding(0, 20, 0, 0);
                scrollView.addView(contenedorLista);
                layoutPrincipal.addView(scrollView);

                // 4. Llenamos la lista con los datos limpios
                List<CheckBox> listaDeCasillas = new ArrayList<>();

                for (Map<String, String> user : usuariosDisponibles) {
                    CheckBox checkBoxUsuario = new CheckBox(getContext());
                    checkBoxUsuario.setText(user.get("display_text"));
                    checkBoxUsuario.setTag(user.get("uid"));

                    contenedorLista.addView(checkBoxUsuario);
                    listaDeCasillas.add(checkBoxUsuario);
                }

                // 5. Lógica del buscador en tiempo real
                inputBuscador.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String filtro = s.toString().toLowerCase();
                        for (CheckBox checkBox : listaDeCasillas) {
                            if (checkBox.getText().toString().toLowerCase().contains(filtro)) {
                                checkBox.setVisibility(View.VISIBLE);
                            } else {
                                checkBox.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });

                // 6. Mostramos el Diálogo
                new AlertDialog.Builder(getContext())
                        .setTitle("Añadir nuevos miembros")
                        .setView(layoutPrincipal)
                        .setPositiveButton("Añadir", (dialog, which) -> {
                            List<String> uidsAAnadir = new ArrayList<>();
                            for (CheckBox checkBox : listaDeCasillas) {
                                if (checkBox.isChecked()) {
                                    uidsAAnadir.add((String) checkBox.getTag());
                                }
                            }

                            if (!uidsAAnadir.isEmpty()) {
                                FirebaseManager.anadirMiembrosAGrupo(idDelGrupoAEditar, uidsAAnadir)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Miembros añadidos con éxito", Toast.LENGTH_SHORT).show();
                                            cargarMisGrupos();
                                        });
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- CREAR GRUPO (VERSIÓN LIMPIA CON BUSCADOR Y SOLO USUARIOS LIBRES) ---
    private void abrirDialogoCrearGrupo() {
        FirebaseManager.obtenerUsuariosLibres(new FirebaseManager.UsuariosDisponiblesCallback() {
            @Override
            public void onUsuariosCargados(List<Map<String, String>> usuariosDisponibles) {

                // 1. Configuramos el Layout Principal
                LinearLayout layoutPrincipal = new LinearLayout(getContext());
                layoutPrincipal.setOrientation(LinearLayout.VERTICAL);
                layoutPrincipal.setPadding(50, 20, 50, 20);

                // 2. Datos del grupo
                final EditText inputNombreGrupo = new EditText(getContext());
                inputNombreGrupo.setHint("Nombre del grupo (ej. Familia)");
                layoutPrincipal.addView(inputNombreGrupo);

                final EditText inputCodigoAcceso = new EditText(getContext());
                inputCodigoAcceso.setHint("Código secreto (ej. 1234)");
                layoutPrincipal.addView(inputCodigoAcceso);

                // 3. Barra de búsqueda
                EditText inputBuscador = new EditText(getContext());
                inputBuscador.setHint("🔍 Buscar por correo...");
                inputBuscador.setPadding(10, 40, 10, 20);
                layoutPrincipal.addView(inputBuscador);

                // 4. Contenedor de la lista
                ScrollView scrollView = new ScrollView(getContext());
                LinearLayout contenedorLista = new LinearLayout(getContext());
                contenedorLista.setOrientation(LinearLayout.VERTICAL);
                scrollView.addView(contenedorLista);
                layoutPrincipal.addView(scrollView);

                // 5. Llenamos la lista con los Checkboxes
                List<CheckBox> listaDeCasillas = new ArrayList<>();

                for (Map<String, String> user : usuariosDisponibles) {
                    CheckBox checkBoxUsuario = new CheckBox(getContext());
                    checkBoxUsuario.setText(user.get("display_text"));
                    checkBoxUsuario.setTag(user.get("uid"));

                    contenedorLista.addView(checkBoxUsuario);
                    listaDeCasillas.add(checkBoxUsuario);
                }

                // 6. Lógica del buscador
                inputBuscador.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String filtro = s.toString().toLowerCase();
                        for (CheckBox checkBox : listaDeCasillas) {
                            if (checkBox.getText().toString().toLowerCase().contains(filtro)) {
                                checkBox.setVisibility(View.VISIBLE);
                            } else {
                                checkBox.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });

                // 7. Mostramos el Diálogo
                new AlertDialog.Builder(getContext())
                        .setTitle("Crear Nuevo Grupo")
                        .setView(layoutPrincipal)
                        .setPositiveButton("Crear", (dialog, which) -> {
                            String nombreGrupo = inputNombreGrupo.getText().toString().trim();
                            String codigoAcceso = inputCodigoAcceso.getText().toString().trim();

                            if (nombreGrupo.isEmpty() || codigoAcceso.isEmpty()) {
                                Toast.makeText(getContext(), "Rellena nombre y código", Toast.LENGTH_LONG).show();
                            } else {
                                List<String> uidsSeleccionados = new ArrayList<>();
                                for (CheckBox checkBox : listaDeCasillas) {
                                    if (checkBox.isChecked()) {
                                        uidsSeleccionados.add((String) checkBox.getTag());
                                    }
                                }

                                FirebaseManager.crearGrupo(nombreGrupo, codigoAcceso, uidsSeleccionados)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "¡Grupo creado!", Toast.LENGTH_SHORT).show();
                                            cargarMisGrupos();
                                        });
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }
}