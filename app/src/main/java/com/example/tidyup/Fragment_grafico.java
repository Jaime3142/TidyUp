package com.example.tidyup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
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
        View view = inflater.inflate(R.layout.fragment_grafico, container, false);

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

        return view;
    }

    private void mostrarTipAleatorio() {
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
    }

    private void cargarEstadisticasGenerales() {
        db.collection("Tareas")
                .whereEqualTo("asignada", userEmail)
                .get()
                .addOnCompleteListener(task -> {
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
                });
    }

    private void actualizarInterfaz(int total, int completadas) {
        if (!isAdded()) return;

        if (total == 0) {
            tvPorcentajeEficiencia.setText("0");
            tvRatioTareas.setText("0 / 0");
            progressBarTareas.setProgress(0);
            tvFeedbackEficiencia.setText("No tienes tareas asignadas");
            return;
        }

        // Cálculo de eficiencia
        int eficiencia = (int) (((float) completadas / total) * 100);

        tvPorcentajeEficiencia.setText(String.valueOf(eficiencia));
        tvRatioTareas.setText(completadas + " / " + total);

        progressBarTareas.setMax(total);
        progressBarTareas.setProgress(completadas);

        // Cambiar colores según porcentaje
        if (eficiencia >= 80) {
            tvPorcentajeEficiencia.setTextColor(Color.parseColor("#4CAF50")); // Verde
            tvFeedbackEficiencia.setText("Nivel: ¡Excelente rendimiento!");
        } else if (eficiencia >= 50) {
            tvPorcentajeEficiencia.setTextColor(Color.parseColor("#FF9800")); // Naranja
            tvFeedbackEficiencia.setText("Nivel: Buen ritmo");
        } else {
            tvPorcentajeEficiencia.setTextColor(Color.parseColor("#F44336")); // Rojo
            tvFeedbackEficiencia.setText("Nivel: Necesitas mejorar");
        }
    }
}