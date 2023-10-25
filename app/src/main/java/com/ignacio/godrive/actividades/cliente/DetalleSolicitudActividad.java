package com.ignacio.godrive.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.utilidades.DecodePoints;
import com.ignacio.godrive.includes.MyToolBar;
import com.ignacio.godrive.proveedores.ProveedoresGoogleApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleSolicitudActividad extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private SupportMapFragment mapaFragmento;
    private double extraOrigenLat;
    private double extraOrigenLng;
    private double extraDestinoLat;
    private double extraDestinoLng;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private ProveedoresGoogleApi proveedoresGoogleApi;
    private List<LatLng> PolyLineList;
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_solicitud_actividad);
        MyToolBar.show(this, "DATOS DE VIAJE", true);

        mapaFragmento = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        mapaFragmento.getMapAsync(this);
        extraOrigenLat = getIntent().getDoubleExtra("origen_lat", 0);
        extraOrigenLng = getIntent().getDoubleExtra("origen_lng", 0);
        extraDestinoLat = getIntent().getDoubleExtra("destino_lat", 0);
        extraDestinoLng = getIntent().getDoubleExtra("destino_lng", 0);

        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);
        proveedoresGoogleApi = new ProveedoresGoogleApi(DetalleSolicitudActividad.this);
    }

    private void marcadorRuta(){
        proveedoresGoogleApi.getDirections(origenLatLng, destinoLatLng).enqueue(new Callback<String>() {
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
                    polylineOptions.width(8f);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(PolyLineList);
                    mapa.addPolyline(polylineOptions);

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

        mapa.addMarker(new MarkerOptions().position(origenLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin)));
        mapa.addMarker(new MarkerOptions().position(destinoLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_pin)));
        mapa.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(origenLatLng)
                        .zoom(14f)
                        .build()
        ));
        marcadorRuta();
    }
}