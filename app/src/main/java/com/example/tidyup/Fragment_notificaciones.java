package com.example.tidyup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Fragment_notificaciones extends Fragment {

    private LinearLayout contenedorNotificaciones;
    private ListenerRegistration listenerNotificaciones; // para poder cancelarlo

    public Fragment_notificaciones() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notificaciones, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contenedorNotificaciones = view.findViewById(R.id.contenedorNotificaciones);
        escucharNotificaciones();
    }

    private void escucharNotificaciones() {
        listenerNotificaciones = FirebaseManager.escucharNotificaciones((snapshots, error) -> {

            if (!isAdded() || getActivity() == null) return;
            if (error != null || snapshots == null) return;

            requireActivity().runOnUiThread(() -> {

                // LOG para verificar
                android.util.Log.d("NOTIF", "runOnUiThread ejecutado");
                android.util.Log.d("NOTIF", "contenedor es null: " + (contenedorNotificaciones == null));

                if (contenedorNotificaciones == null) return; // protección

                contenedorNotificaciones.removeAllViews();

                LayoutInflater inflater = LayoutInflater.from(requireContext());

                for (QueryDocumentSnapshot doc : snapshots) {
                    String idNotif = doc.getId();
                    String mensaje = doc.getString("mensaje");

                    android.util.Log.d("NOTIF", "Inflando tarjeta: " + mensaje);

                    View tarjeta = inflater.inflate(R.layout.item_notificacion, null);

                    TextView tvMensaje      = tarjeta.findViewById(R.id.tvMensajeNotif);
                    ImageButton btnEliminar = tarjeta.findViewById(R.id.btnEliminarNotif);

                    if (tvMensaje != null) tvMensaje.setText(mensaje);
                    else android.util.Log.e("NOTIF", "tvMensajeNotif es null — revisa el id en item_notificacion.xml");

                    if (btnEliminar != null) {
                        btnEliminar.setOnClickListener(v ->
                                FirebaseManager.eliminarNotificacion(idNotif));
                    }

                    contenedorNotificaciones.addView(tarjeta);
                    android.util.Log.d("NOTIF", "Tarjeta añadida al contenedor");
                }
            });
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Cancelamos el listener cuando salimos para no desperdiciar recursos
        if (listenerNotificaciones != null) {
            listenerNotificaciones.remove();
        }
    }
}