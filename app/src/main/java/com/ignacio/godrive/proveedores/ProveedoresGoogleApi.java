package com.ignacio.godrive.proveedores;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.retrofit.IGoogleApi;
import com.ignacio.godrive.actividades.retrofit.RetrofitCliente;

import java.util.Date;

import retrofit2.Call;

public class ProveedoresGoogleApi {

    private Context context;
    public ProveedoresGoogleApi(Context context){
        this.context = context;
    }

    public Call<String> getDirections(LatLng origenLatLng, LatLng destinoLatLng){
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
        + "origin=" + origenLatLng.latitude + "," + origenLatLng.longitude + "&"
        + "destination=" + destinoLatLng.latitude + "," + destinoLatLng.longitude + "&"
        + "departure_time=" + (new Date().getTime() + (60*60*1000)) + "&"
        + "traffic_model=best_guess&"
        + "key=" + context.getResources().getString(R.string.google_maps_key);

        return RetrofitCliente.obtenerCliente(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
