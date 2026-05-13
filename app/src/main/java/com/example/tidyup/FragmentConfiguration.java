package com.example.tidyup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class FragmentConfiguration extends Fragment {

    public FragmentConfiguration() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        Button btnLogout = view.findViewById(R.id.button6);
        Button btnGuide = view.findViewById(R.id.button10);

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

        btnGuide.setOnClickListener(v -> {
            String url = "https://haixiao69.gitbook.io/manual-de-usuario-de-tidyup";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }
}