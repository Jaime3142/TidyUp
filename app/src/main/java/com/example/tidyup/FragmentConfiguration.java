package com.example.tidyup;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class FragmentConfiguration extends Fragment {

    public FragmentConfiguration() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        Button btnPrivacy = view.findViewById(R.id.button5);
        Button btnLogout = view.findViewById(R.id.button6);
        Button btnHelp = view.findViewById(R.id.button7);
        Button btnConsent = view.findViewById(R.id.button8);
        Button btnGuide = view.findViewById(R.id.button10);

        btnPrivacy.setOnClickListener(v -> mostrarPopupCreditos());

        btnLogout.setOnClickListener(v -> {
            FirebaseManager.cerrarSesion();
            Toast.makeText(getContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), ActivityLogin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        btnHelp.setOnClickListener(v -> {
            String url = "https://jaime3142.github.io/paginaweb/pwTidyUp/";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        btnConsent.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        btnGuide.setOnClickListener(v -> {
            String url = "https://haixiao69.gitbook.io/manual-de-usuario-de-tidyup";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }

    private void mostrarPopupCreditos() {
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        String[] creditos = {
                "Diseño general de la app:\nRaúl Garzón Pérez",
                "Diseño de Adolescentes:\nIván García",
                "Diseño de Adultos:\nHaixiao Wang",
                "Diseño de Ancianos:\nJaime Navas",
                "Paleta de colores y logo:\nLaura Barrionuevo Martínez",
                "© " + anioActual + " TidyUp.\nTodos los derechos reservados.\nQueda prohibida su copia o distribución sin autorización."
        };

        TextView tvCreditos = new TextView(getContext());
        tvCreditos.setTextSize(18);
        tvCreditos.setPadding(50, 80, 50, 80);
        tvCreditos.setGravity(Gravity.CENTER);
        tvCreditos.setTypeface(null, Typeface.BOLD);
        tvCreditos.setTextColor(getResources().getColor(android.R.color.white));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Créditos de TidyUp")
                .setView(tvCreditos)
                .setPositiveButton("Cerrar", null)
                .setCancelable(false)
                .create();

        dialog.show();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            int index = 0;

            @Override
            public void run() {
                if (dialog.isShowing()) {
                    tvCreditos.setText(creditos[index]);
                    index++;

                    if (index < creditos.length) {
                        handler.postDelayed(this, 2500);
                    }
                }
            }
        };

        handler.post(runnable);
    }
}