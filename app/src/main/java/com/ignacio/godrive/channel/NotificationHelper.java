package com.ignacio.godrive.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.ignacio.godrive.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.ignacio.godrive";
    private static final String CHANNEL_NAME = "GoDrive";

    private NotificationManager manager;
    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new
                NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String titulo, String contenido, PendingIntent intent, Uri sonidoUri) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(contenido).setBigContentTitle(titulo));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationActions(String titulo, String contenido, Uri sonidoUri, Notification.Action acceptAction, Notification.Action cancelAction) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(contenido).setBigContentTitle(titulo));
    }

    public NotificationCompat.Builder getNotificationOldAPI(String titulo, String contenido, PendingIntent intent, Uri sonidoUri) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contenido).setBigContentTitle(titulo));
    }

    public NotificationCompat.Builder getNotificationOldAPIActions(String titulo, String contenido, Uri sonidoUri, NotificationCompat.Action acceptAction, NotificationCompat.Action cancelAction) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(sonidoUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contenido).setBigContentTitle(titulo));
    }
}
