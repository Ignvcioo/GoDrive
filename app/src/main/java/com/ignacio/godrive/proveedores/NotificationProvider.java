package com.ignacio.godrive.proveedores;

import com.ignacio.godrive.modelo.FCMBody;
import com.ignacio.godrive.modelo.FCMResponse;
import com.ignacio.godrive.retrofit.IFCMApi;
import com.ignacio.godrive.retrofit.RetrofitCliente;

import retrofit2.Call;

public class NotificationProvider {
    private String url ="https://fcm.googleapis.com";

    public NotificationProvider() {

    }

    public Call<FCMResponse> sendNotification(FCMBody contenido) {
        return RetrofitCliente.getClientObject(url).create(IFCMApi.class).send(contenido);
    }
}
