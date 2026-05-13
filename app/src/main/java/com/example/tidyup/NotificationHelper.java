package com.example.tidyup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// Esta clase se encarga de mostrar notificaciones en el movil del usuario.
// Se usa cada vez que alguien completa una tarea.
public class NotificationHelper {

    // Identificador interno del canal de notificaciones, tiene que ser unico en la app
    private static final String CHANNEL_ID   = "tidyup_tareas";
    // Nombre que ve el usuario si va a ajustes del movil y mira las notificaciones de la app
    private static final String CHANNEL_NAME = "Tareas completadas";

    // Muestra una notificacion en el movil con el titulo de la tarea y los puntos ganados.
    public static void mostrarNotificacion(Context context, String tituloTarea, int puntos) {

        // A partir de Android 8 es obligatorio crear un canal antes de poder
        // mandar notificaciones, si ya existe no pasa nada, no se duplica
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "tidyup_tareas",
                    "Tareas completadas",
                    NotificationManager.IMPORTANCE_HIGH); // HIGH hace que aparezca como popup en pantalla
            canal.setDescription("Avisos al completar tareas");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(canal);
        }

        // Construye la notificacion con el icono, titulo y texto que va a ver el usuario
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "tidyup_tareas")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tarea completada")
                .setContentText("Has completado \"" + tituloTarea + "\" y ganado " + puntos + " puntos")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH para que salga como popup y no solo en la barra
                .setAutoCancel(true); // Se cierra sola cuando el usuario la toca

        // Intenta lanzar la notificacion.
        // Si el usuario no ha dado permiso de notificaciones lanza una excepcion
        // que se captura para que la app no se cierre por error.
        try {
            NotificationManagerCompat.from(context)
                    .notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}