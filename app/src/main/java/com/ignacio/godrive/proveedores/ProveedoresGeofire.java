package com.ignacio.godrive.proveedores;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProveedoresGeofire {

    private DatabaseReference baseDatos;
    private GeoFire geofire;

    public ProveedoresGeofire(String reference){
        baseDatos = FirebaseDatabase.getInstance().getReference().child(reference);
        geofire = new GeoFire(baseDatos);
    }

    public void GuardarLocalicacion(String idConductor, LatLng latLng){
        geofire.setLocation(idConductor, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void EliminarLocalicacion(String idConductor){
        geofire.removeLocation(idConductor);
    }

    public GeoQuery ubicacionConductores(LatLng latLng, double radio){
        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radio);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference isDriverWorking(String idDriver) {
        return FirebaseDatabase.getInstance().getReference().child("Conductores_Trabajando").child(idDriver);
    }
}
