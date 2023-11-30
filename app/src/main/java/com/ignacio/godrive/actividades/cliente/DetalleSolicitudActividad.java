package com.ignacio.godrive.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.ignacio.godrive.utils.DecodePoints;
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
    private String extraOrigen;
    private String extraDestino;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private ProveedoresGoogleApi proveedoresGoogleApi;
    private List<LatLng> PolyLineList;
    private PolylineOptions polylineOptions;
    private TextView textViewOrigen;
    private TextView textViewDestino;
    private TextView textViewTiempo;
    private TextView textViewDistancia;
    private Button btnSolicitarViaje;

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
        extraOrigen = getIntent().getStringExtra("origen");
        extraDestino = getIntent().getStringExtra("destino");

        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);
        proveedoresGoogleApi = new ProveedoresGoogleApi(DetalleSolicitudActividad.this);

        textViewOrigen = findViewById(R.id.txtOrigen);
        textViewDestino = findViewById(R.id.txtDestino);
        textViewTiempo = findViewById(R.id.txtTiempo);
        textViewDistancia = findViewById(R.id.txtDistancia);
        btnSolicitarViaje = findViewById(R.id.btnViaje);

        textViewOrigen.setText(extraOrigen);
        textViewDestino.setText(extraDestino);

        btnSolicitarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solicitarConductor();
            }
        });
    }

    private void solicitarConductor() {
        Intent intent = new Intent(DetalleSolicitudActividad.this, SolicitarConductorActividad.class);
        intent.putExtra("origin_lat", origenLatLng.latitude);
        intent.putExtra("origin_lng", origenLatLng.longitude);
        intent.putExtra("origin", extraOrigen);
        intent.putExtra("destination", extraDestino);
        intent.putExtra("destination_lat", destinoLatLng.latitude);
        intent.putExtra("destination_lng", destinoLatLng.longitude);
        startActivity(intent);
        finish();
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
                   textViewTiempo.setText(duracionTexto);
                   textViewDistancia.setText(distanciaTexto);
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
                        .zoom(16f)
                        .build()
        ));
        marcadorRuta();
    }
}