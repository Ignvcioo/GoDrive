package com.ignacio.godrive.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.conductor.MapDriverBookingActivity;
import com.ignacio.godrive.proveedores.ClientBookingProvider;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresConductores;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;
import com.ignacio.godrive.proveedores.ProveedoresGoogleApi;
import com.ignacio.godrive.proveedores.TokenProvider;
import com.ignacio.godrive.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    ProveedoresAutenticacion proveedoresAutenticacion;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragmento;

    private ProveedoresGeofire geofireProveedores;
    private TokenProvider mTokenProvider;
    private Marker marcadorConductor;
    private ClientBookingProvider mClientBookingProvider;
    private boolean primeraVez = true;

    private PlacesClient placesClient;
    private String nombreOrigen;
    private LatLng origenLatLng;
    private String nombreDestino;
    private LatLng destinoLatLng;
    private LatLng driverLatLng;
    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewStatusBooking;
    private ProveedoresGoogleApi proveedoresGoogleApi;
    private List<LatLng> PolyLineList;
    private PolylineOptions polylineOptions;
    private ProveedoresConductores mDriverProvider;
    private  ValueEventListener mListener;
    private String mIdDriver;
    private ValueEventListener mListenerStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        proveedoresAutenticacion = new ProveedoresAutenticacion();
        geofireProveedores = new ProveedoresGeofire("Conductores_Trabajando");
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mDriverProvider = new ProveedoresConductores();
        proveedoresGoogleApi = new ProveedoresGoogleApi(MapClientBookingActivity.this);
        mapaFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaFragmento.getMapAsync(this);



        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }


        mTextViewClientBooking = findViewById(R.id.textViewDriverBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailDriverBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginDriverBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationDriverBooking);
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking);

        getStatus();
        getClientBooking();
    }

    private void getStatus() {
        mListenerStatus = mClientBookingProvider.getStatus(proveedoresAutenticacion.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")) {
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if (status.equals("start")) {
                        mTextViewStatusBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    }
                    else if (status.equals("finish")) {
                        mTextViewStatusBooking.setText("Estado: Viaje Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        mapa.clear();
        mapa.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin)));
        marcadorRuta(destinoLatLng);
    }

    private void getClientBooking() {
         mClientBookingProvider.getClientBooking(proveedoresAutenticacion.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origen = dataSnapshot.child("origin").getValue().toString();
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;
                    double destinoLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    origenLatLng = new LatLng(originLat, originLng);
                    destinoLatLng = new LatLng(destinoLat, destinoLng);
                    mTextViewOriginClientBooking.setText("Origen: " + origen);
                    mTextViewDestinationClientBooking.setText("Destino: " + destino);
                    mapa.addMarker(new MarkerOptions().position(origenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null){
            geofireProveedores.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }
        if (mListenerStatus != null) {
            mClientBookingProvider.getStatus(proveedoresAutenticacion.getId()).removeEventListener(mListener);
        }
    }

    private void getDriver(String idDriver) {
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    mTextViewClientBooking.setText("Conductor: " + name);
                    mTextViewEmailClientBooking.setText("Email del conductor: " + email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
        mListener =  geofireProveedores.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                    driverLatLng = new LatLng(lat, lng);
                    if (marcadorConductor != null) {
                        marcadorConductor.remove();
                    }
                    marcadorConductor = mapa.addMarker(new MarkerOptions().position(
                                    new LatLng(lat,lng)
                            )
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_auto)));
                    if (primeraVez) {
                        primeraVez = false;
                        mapa.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(driverLatLng)
                                        .zoom(16f)
                                        .build()
                        ));
                        marcadorRuta(origenLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void marcadorRuta(LatLng latLng){
        proveedoresGoogleApi.getDirections(driverLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    PolyLineList = DecodePoints.decodePoly(points);
                    polylineOptions = new PolylineOptions();
                    polylineOptions.color(Color.DKGRAY);
                    polylineOptions.width(18f);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(PolyLineList);
                    mapa.addPolyline(polylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaTexto = distance.getString("text");
                    String duracionTexto = duration.getString("text");

                } catch (Exception e){
                    Log.d("Error", "Mensaje de error:" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);


    }
}