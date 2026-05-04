package com.example.tidyup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID   = "tidyup_tareas";
    private static final String CHANNEL_NAME = "Tareas completadas";

    public static void mostrarNotificacion(Context context, String tituloTarea, int puntos) {

        // PASO 1 - Crear canal (obligatorio Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    "tidyup_tareas",
                    "Tareas completadas",
                    NotificationManager.IMPORTANCE_HIGH); // HIGH para que aparezca en pantalla
            canal.setDescription("Avisos al completar tareas");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(canal);
        }

        // PASO 2 - Construir y lanzar
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "tidyup_tareas")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("¡Tarea completada!")
                .setContentText("Has completado \"" + tituloTarea + "\" y ganado " + puntos + " puntos")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // HIGH para que aparezca como popup
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
