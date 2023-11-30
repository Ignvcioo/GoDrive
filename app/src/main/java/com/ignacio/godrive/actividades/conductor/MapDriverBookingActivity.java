package com.ignacio.godrive.actividades.conductor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.cliente.DetalleSolicitudActividad;
import com.ignacio.godrive.proveedores.ClientBookingProvider;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresClientes;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;
import com.ignacio.godrive.proveedores.ProveedoresGoogleApi;
import com.ignacio.godrive.proveedores.TokenProvider;
import com.ignacio.godrive.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    ProveedoresAutenticacion proveedoresAutenticacion;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragmento;
    private LocationRequest locationRequest;
    private ProveedoresGeofire geofireProveedores;
    private ProveedoresClientes mClientProvider;
    private ClientBookingProvider mClientBookingProvider;
    private TokenProvider mTokenProvider;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker marcadorConductor;
    private LatLng actualLatLng;
    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private String mExtraClientId;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private ProveedoresGoogleApi proveedoresGoogleApi;
    private List<LatLng> PolyLineList;
    private PolylineOptions polylineOptions;
    private boolean primeraVez = true;

    // Callback para manejar actualizaciones de ubicación
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (marcadorConductor != null){
                        marcadorConductor.remove();
                    }

                    marcadorConductor = mapa.addMarker(new MarkerOptions().position(
                                            new LatLng(location.getLatitude(), location.getLongitude())
                                    )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_auto))
                    );

                    // Actualiza la cámara del mapa a la ubicación actual
                    mapa.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    updateLocacion();
                    if (primeraVez) {
                        primeraVez = false;
                        getClientBooking();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);
        proveedoresAutenticacion = new ProveedoresAutenticacion();
        geofireProveedores = new ProveedoresGeofire("Conductores_Trabajando");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ProveedoresClientes();
        mClientBookingProvider = new ClientBookingProvider();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapaFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaFragmento.getMapAsync(this);
        mTextViewClientBooking = findViewById(R.id.textViewClientBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        mExtraClientId = getIntent().getStringExtra("idClient");
        proveedoresGoogleApi = new ProveedoresGoogleApi(MapDriverBookingActivity.this);
        getClient();

    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String destino = dataSnapshot.child("destination").getValue().toString();
                    String origen = dataSnapshot.child("origin").getValue().toString();
                    double destinoLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    origenLatLng = new LatLng(originLat, originLng);
                    destinoLatLng = new LatLng(destinoLat, destinoLng);
                    mTextViewOriginClientBooking.setText("Recoger en: " + origen);
                    mTextViewDestinationClientBooking.setText("Destino" + destino);
                    mapa.addMarker(new MarkerOptions().position(origenLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin)));
                    marcadorRuta();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void marcadorRuta(){
        proveedoresGoogleApi.getDirections(actualLatLng, origenLatLng).enqueue(new Callback<String>() {
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

    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    String nombre = dataSnapshot.child("nombre").getValue().toString();
                    mTextViewClientBooking.setText(nombre);
                    mTextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLocacion(){
        if (proveedoresAutenticacion.sesionExistente() && actualLatLng != null){
            geofireProveedores.GuardarLocalicacion(proveedoresAutenticacion.getId(), actualLatLng);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapa.setMyLocationEnabled(false);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        startLocation();


    }

    // Maneja las respuestas a las solicitudes de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()){
                        // Solicita actualizaciones de ubicación
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                    else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    // Maneja el resultado de la actividad de configuración de GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        else{
            showAlertDialogNOGPS();
        }
    }


    // Muestra un cuadro de diálogo para activar el GPS
    private void showAlertDialogNOGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }


    // Verifica si el GPS está activado
    private boolean gpsActived(){
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true;
        }
        return isActive;
    }

    private void desconectado(){

        if (marcadorConductor != null){
            marcadorConductor.remove();
        }
        if (fusedLocationProviderClient != null){

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            if (proveedoresAutenticacion.sesionExistente()){
                geofireProveedores.EliminarLocalicacion(proveedoresAutenticacion.getId());
            }
        }
        else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }


    // Verifica si el GPS está activado
    private void startLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (gpsActived()){
                    // Solicita actualizaciones de ubicación
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        }
        else {
            if (gpsActived()){
                // Solicita actualizaciones de ubicación
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    // Verifica y solicita permisos de ubicación
    private void checkLocationPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Solicita permisos de ubicación
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                // Solicita permisos de ubicación
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);            }
        }
    }
}