package com.example.tidyup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Fragment_grafico extends Fragment {

    private TextView tvPorcentajeEficiencia, tvFeedbackEficiencia, tvRatioTareas, tvMensajeAnimo, tvTipDia;
    private ProgressBar progressBarTareas;
    private FirebaseFirestore db;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;

        try {
            view = inflater.inflate(R.layout.fragment_grafico, container, false);

            // Enlazar vistas con el XML
            tvPorcentajeEficiencia = view.findViewById(R.id.tvPorcentajeEficiencia);
            tvFeedbackEficiencia = view.findViewById(R.id.tvFeedbackEficiencia);
            tvRatioTareas = view.findViewById(R.id.tvRatioTareas);
            progressBarTareas = view.findViewById(R.id.progressBarTareas);
            tvMensajeAnimo = view.findViewById(R.id.tvMensajeAnimo);
            tvTipDia = view.findViewById(R.id.tvTipDia);

            // Iniciar la lógica de consejos aleatorios
            mostrarTipAleatorio();

            db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                userEmail = currentUser.getEmail();
                cargarEstadisticasGenerales();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al cargar la pantalla de estadísticas", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void mostrarTipAleatorio() {
        try {
            List<String> consejos = new ArrayList<>();
            consejos.add("Divide y vencerás: divide las tareas grandes en subtareas.");
            consejos.add("La Regla de los 2 Minutos: si toma menos de 2 min, hazlo ya.");
            consejos.add("Técnica Pomodoro: trabaja 25 min y descansa 5.");
            consejos.add("Bloqueo de Tiempo: reserva bloques para tareas cruciales.");
            consejos.add("Cómete ese sapo: haz lo más difícil primero.");
            consejos.add("Desconecta para conectar: apaga notificaciones mientras trabajas.");
            consejos.add("Revisa tu progreso: planifica mañana al terminar hoy.");

            Collections.shuffle(consejos);
            if (!consejos.isEmpty() && tvTipDia != null) {
                tvTipDia.setText(consejos.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarEstadisticasGenerales() {
        try {
            db.collection("Tareas")
                    .whereEqualTo("asignada", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        try {
                            if (task.isSuccessful() && task.getResult() != null) {
                                int total = 0;
                                int completadas = 0;

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    total++;
                                    String estado = document.getString("estado");
                                    if (estado != null && estado.equalsIgnoreCase("completada")) {
                                        completadas++;
                                    }
                                }
                                actualizarInterfaz(total, completadas);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void actualizarInterfaz(int total, int completadas) {
        try {
            if (!isAdded()) return;

            if (total == 0) {
                if (tvPorcentajeEficiencia != null) tvPorcentajeEficiencia.setText("0");
                if (tvRatioTareas != null) tvRatioTareas.setText("0 / 0");
                if (progressBarTareas != null) progressBarTareas.setProgress(0);
                if (tvFeedbackEficiencia != null) tvFeedbackEficiencia.setText("No tienes tareas asignadas");
                return;
            }

            // Cálculo de eficiencia
            int eficiencia = (int) (((float) completadas / total) * 100);

            if (tvPorcentajeEficiencia != null) tvPorcentajeEficiencia.setText(String.valueOf(eficiencia));
            if (tvRatioTareas != null) tvRatioTareas.setText(completadas + " / " + total);

            if (progressBarTareas != null) {
                progressBarTareas.setMax(total);
                progressBarTareas.setProgress(completadas);
            }

            // Cambiar colores según porcentaje
            if (eficiencia >= 80) {
                if (tvPorcentajeEficiencia != null) tvPorcentajeEficiencia.setTextColor(Color.parseColor("#4CAF50")); // Verde
                if (tvFeedbackEficiencia != null) tvFeedbackEficiencia.setText("Nivel: ¡Excelente rendimiento!");
            } else if (eficiencia >= 50) {
                if (tvPorcentajeEficiencia != null) tvPorcentajeEficiencia.setTextColor(Color.parseColor("#FF9800")); // Naranja
                if (tvFeedbackEficiencia != null) tvFeedbackEficiencia.setText("Nivel: Buen ritmo");
            } else {
                if (tvPorcentajeEficiencia != null) tvPorcentajeEficiencia.setTextColor(Color.parseColor("#F44336")); // Rojo
                if (tvFeedbackEficiencia != null) tvFeedbackEficiencia.setText("Nivel: Necesitas mejorar");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}