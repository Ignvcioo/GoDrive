package com.ignacio.godrive.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ignacio.godrive.actividades.conductor.MapDriverBookingActivity;
import com.ignacio.godrive.proveedores.ClientBookingProvider;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;

public class AcceptReceiver extends BroadcastReceiver {
    private ClientBookingProvider mClientBookingProvider;
    private ProveedoresGeofire mGeofireProvider;
    private ProveedoresAutenticacion mAuthProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new ProveedoresAutenticacion();
        mGeofireProvider = new ProveedoresGeofire("Conductores_Activos");
        mGeofireProvider.EliminarLocalicacion(mAuthProvider.getId());
        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "accept");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", idClient);
        context.startActivity(intent1);
    }
}
