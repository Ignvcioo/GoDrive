package com.ignacio.godrive.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.ignacio.godrive.R;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;
import com.ignacio.godrive.proveedores.TokenProvider;

import java.util.ArrayList;
import java.util.List;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    ProveedoresAutenticacion proveedoresAutenticacion;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragmento;

    private ProveedoresGeofire geofireProveedores;
    private TokenProvider mTokenProvider;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private LatLng actualLatLng;
    private List<Marker> conductoresMarker = new ArrayList<>();
    private boolean primeraVez = true;

    private PlacesClient placesClient;
    private String nombreOrigen;
    private LatLng origenLatLng;
    private String nombreDestino;
    private LatLng destinoLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        proveedoresAutenticacion = new ProveedoresAutenticacion();
        geofireProveedores = new ProveedoresGeofire("Conductores_Trabajando");
        mTokenProvider = new TokenProvider();
        mapaFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaFragmento.getMapAsync(this);



        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        placesClient = Places.createClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);


    }
}