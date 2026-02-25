package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UltRecordatorio#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UltRecordatorio extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public UltRecordatorio() {
        // Required empty public constructor
    }

    public static UltRecordatorio newInstance(String param1, String param2) {
        UltRecordatorio fragment = new UltRecordatorio();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 1. Inflamos el diseño
        View root = inflater.inflate(R.layout.fragment_ult_recordatorio, container, false);

        // 2. Buscamos el ImageButton de la flecha (según tu captura es imageButton6)
        ImageButton btnAtras = root.findViewById(R.id.imageButton6);

        // 3. Programamos el clic para volver atrás
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Esto cierra el fragmento actual y vuelve al anterior
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        return root;
    }
}