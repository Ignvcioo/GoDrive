package com.ignacio.godrive.actividades.cliente;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.PrincipalActividad;
import com.ignacio.godrive.includes.MyToolBar;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;
import com.ignacio.godrive.proveedores.TokenProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapaClienteActividad extends AppCompatActivity implements OnMapReadyCallback {

    ProveedoresAutenticacion proveedoresAutenticacion;
    private GoogleMap mapa;
    private SupportMapFragment mapaFragmento;
    private LocationRequest locationRequest;
    private ProveedoresGeofire geofireProveedores;
    private TokenProvider mTokenProvider;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private LatLng actualLatLng;
    private List<Marker> conductoresMarker = new ArrayList<>();
    private boolean primeraVez = true;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private AutocompleteSupportFragment autocompleteSupportFragmentDestino;
    private PlacesClient placesClient;
    private String nombreOrigen;
    private LatLng origenLatLng;
    private String nombreDestino;
    private LatLng destinoLatLng;
    private  GoogleMap.OnCameraIdleListener camera;
    private Button btnSolicitarConductor;

    // Callback para manejar actualizaciones de ubicación
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    actualLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    /*

                    if (marcadorCliente != null){
                        marcadorCliente.remove();
                    }

                    marcadorCliente = mapa.addMarker(new MarkerOptions().position(
                                            new LatLng(location.getLatitude(), location.getLongitude())
                                    )
                                    .title("Tu posicion")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_cliente))
                    );
                    */

                    // Actualiza la cámara del mapa a la ubicación actual
                    mapa.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));

                    if (primeraVez) {
                        primeraVez = false;
                        ubicacionConductores();
                        limitarBusqueda();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_mapa_cliente_actividad);
        // Muestra la barra de herramientas personalizada
        MyToolBar.show(this, "Cliente", false);

        proveedoresAutenticacion = new ProveedoresAutenticacion();
        geofireProveedores = new ProveedoresGeofire("Conductores_Activos");
        mTokenProvider = new TokenProvider();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapaFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaFragmento.getMapAsync(this);
        btnSolicitarConductor = findViewById(R.id.btnSolicitarConductor);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        placesClient = Places.createClient(this);
        instanciaAutocompleteOrigen();
        instanciaAutocompleteDestino();
        onCameraMove();

        generateToken();

        btnSolicitarConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarConductor();
            }
        });
    }

    private void solicitarConductor() {
        if (origenLatLng != null && destinoLatLng != null){
            Intent intent = new Intent(MapaClienteActividad.this, DetalleSolicitudActividad.class);
            intent.putExtra("origen_lat", origenLatLng.latitude);
            intent.putExtra("origen_lng", origenLatLng.longitude);
            intent.putExtra("destino_lat", destinoLatLng.latitude);
            intent.putExtra("destino_lng", destinoLatLng.longitude);
            intent.putExtra("origen", nombreOrigen);
            intent.putExtra("destino", nombreDestino);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Debe seleccionar el lugar de origen y destino.", Toast.LENGTH_SHORT).show();
        }
    }

    private void instanciaAutocompleteOrigen(){
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigen);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragment.setHint("Origen");
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                nombreOrigen = place.getName();
                origenLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + nombreOrigen);
                Log.d("PLACE", "Lat: " + origenLatLng.latitude);
                Log.d("PLACE", "Long: " + origenLatLng.longitude);
            }
        });
    }

    private void instanciaAutocompleteDestino(){
        autocompleteSupportFragmentDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestino);
        autocompleteSupportFragmentDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteSupportFragmentDestino.setHint("Destino");
        autocompleteSupportFragmentDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                nombreDestino = place.getName();
                destinoLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + destinoLatLng);
                Log.d("PLACE", "Lat: " + destinoLatLng.latitude);
                Log.d("PLACE", "Long: " + destinoLatLng.longitude);
            }
        });
    }

    private void limitarBusqueda(){
        LatLng northSide = SphericalUtil.computeOffset(actualLatLng, 5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(actualLatLng, 5000, 180);
        autocompleteSupportFragment.setCountry("CHL");
        autocompleteSupportFragment.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        autocompleteSupportFragmentDestino.setCountry("CHL");
        autocompleteSupportFragmentDestino.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void onCameraMove(){
        camera = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapaClienteActividad.this);
                    origenLatLng = mapa.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(origenLatLng.latitude, origenLatLng.longitude, 1);
                    String direccion = addressList.get(0).getAddressLine(0);
                    nombreOrigen = direccion;
                    autocompleteSupportFragment.setText(direccion);
                } catch (Exception e){
                    Log.d("Error: ", "Mensaje de error: " + e.getMessage());
                }
            }
        };
    }

    private void ubicacionConductores() {
        geofireProveedores.ubicacionConductores(actualLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Añadiremos los marcadores de los conductores que se conecten.
                for (Marker marker : conductoresMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng conductorLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mapa.addMarker(new MarkerOptions().position(conductorLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_auto)));
                marker.setTag(key);
                conductoresMarker.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : conductoresMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            conductoresMarker.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // Actualizar la posicion de cada conductor.
                for (Marker marker : conductoresMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.setOnCameraIdleListener(camera);

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
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mapa.setMyLocationEnabled(true);
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mapa.setMyLocationEnabled(true);
        }
        else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()){
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

    // Verifica si el GPS está activado
    private void startLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (gpsActived()){
                    // Solicita actualizaciones de ubicación
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mapa.setMyLocationEnabled(true);
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mapa.setMyLocationEnabled(true);
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
                                ActivityCompat.requestPermissions(MapaClienteActividad.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                // Solicita permisos de ubicación
                ActivityCompat.requestPermissions(MapaClienteActividad.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cliente_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cerrarSesion){
            cerrarSesion();
        }
        return super.onOptionsItemSelected(item);
    }

    void cerrarSesion(){
        proveedoresAutenticacion.cerrarSesion();
        Intent intent = new Intent(MapaClienteActividad.this, PrincipalActividad.class);
        startActivity(intent);
        finish();
    }

    void generateToken() {
        mTokenProvider.create(proveedoresAutenticacion.getId());
    }
}