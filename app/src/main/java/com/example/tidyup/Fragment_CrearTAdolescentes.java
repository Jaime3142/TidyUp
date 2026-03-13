package com.example.tidyup;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_CrearTAdolescentes#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Fragment_CrearTAdolescentes extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText titulo;
    private Spinner usuario;
    private EditText descripcion;
    private EditText fecha;
    private EditText puntos;

    private Button botonGuardar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_CrearTAdolescentes.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_CrearTAdolescentes newInstance(String param1, String param2) {
        Fragment_CrearTAdolescentes fragment = new Fragment_CrearTAdolescentes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Fragment_CrearTAdolescentes() {
        // Required empty public constructor
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

        // 1. SOLO UN INFLATE: Guardamos la vista en una variable

        View miVista = inflater.inflate(R.layout.fragment__crear_t_adolescentes, container, false);

        // 2. BUSCAMOS LOS COMPONENTES: Ahora que miVista ya tiene el XML cargado
            titulo = miVista.findViewById(R.id.tituloTarea);
            usuario = miVista.findViewById(R.id.nUsuario);
            descripcion = miVista.findViewById(R.id.descripcionTarea);
            fecha = miVista.findViewById(R.id.fecha);
            puntos = miVista.findViewById(R.id.cPuntos);
            botonGuardar = miVista.findViewById(R.id.bCrearT);

            botonGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });






        // 4. EL ÚNICO RETURN: Al final, devolvemos la vista ya configurada
        return miVista;
    }

    private void ejecutarGuardado() {
        // 1. Extraemos el texto de los EditText normales
        String Ttitulo = titulo.getText().toString().trim();
        String Tdesc = descripcion.getText().toString().trim();
        String Tfecha = fecha.getText().toString().trim();

        // 2. CAMBIO AQUÍ: Extraemos el valor seleccionado del Spinner
        String Tusuario = "";
        if (usuario.getSelectedItem() != null) {
            Tusuario = usuario.getSelectedItem().toString();
        }

        // 3. Validación
        if (Ttitulo.isEmpty() || Tusuario.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, rellena los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Llamamos a tu clase de servicio (FirebaseService)
        FirebaseManager.guardarTarea(Ttitulo, Tusuario, Tdesc, Tfecha, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Tarea creada correctamente", Toast.LENGTH_SHORT).show();
                reemplazarFragment(new fragment_Tareas());
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    public void reemplazarFragment(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Reemplazo el contenido del contenedor con el nuevo fragmento
        fragmentTransaction.replace(R.id.fragmentContiner, fragment);

        fragmentTransaction.commit();
    }
}