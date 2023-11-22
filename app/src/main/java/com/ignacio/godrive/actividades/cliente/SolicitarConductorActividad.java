package com.ignacio.godrive.actividades.cliente;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.ignacio.godrive.R;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;

public class SolicitarConductorActividad extends AppCompatActivity {

    private LottieAnimationView animationView;
    private TextView mTextViewLookingFor;
    private Button btnCancelarViaje;
    private ProveedoresGeofire proveedoresGeofire;
    private double extraOrigenLat;
    private double extraOrigenLng;
    private LatLng origenLatLng;
    private double radio = 0.1;
    private boolean conductorEncontrado = false;
    private String idConductorEncontrado = "";
    private LatLng conductorEncontradoLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_conductor_actividad);

        animationView = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        animationView.playAnimation();
        extraOrigenLat = getIntent().getDoubleExtra("origin_lat", 0);
        extraOrigenLng = getIntent().getDoubleExtra("origin_lng", 0);
        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);

        proveedoresGeofire = new ProveedoresGeofire();
        obtenerConductorCercano();
    }

    private void obtenerConductorCercano() {
        proveedoresGeofire.ubicacionConductores(origenLatLng, radio).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!conductorEncontrado) {
                    conductorEncontrado = true;
                    idConductorEncontrado = key;
                    conductorEncontradoLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("Conductor encontrado\nEsperando respuesta...");
                    Log.d("Conductor", "ID" + idConductorEncontrado);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                // INGRESA CUANDO TERMINA LA BUSQUEDA DE CONDUCTOR EN UN RADIO DE 0.1 KM
                if (!conductorEncontrado) {
                    radio = radio + 0.1f;
                    // NO ENCONTRO NINGUN CONDUCTOR
                    if (radio > 5) {
                        mTextViewLookingFor.setText("No se encontro ningun conductor");
                        Toast.makeText(SolicitarConductorActividad.this, "No se encontro ningun conductor", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else {
                        obtenerConductorCercano();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}