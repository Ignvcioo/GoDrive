package com.ignacio.godrive.actividades.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ignacio.godrive.R;
import com.ignacio.godrive.modelo.ClientBooking;
import com.ignacio.godrive.modelo.FCMBody;
import com.ignacio.godrive.modelo.FCMResponse;
import com.ignacio.godrive.proveedores.ClientBookingProvider;
import com.ignacio.godrive.proveedores.NotificationProvider;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresGeofire;
import com.ignacio.godrive.proveedores.ProveedoresGoogleApi;
import com.ignacio.godrive.proveedores.TokenProvider;
import com.ignacio.godrive.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.AuthProvider;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitarConductorActividad extends AppCompatActivity {

    private LottieAnimationView animationView;
    private TextView mTextViewLookingFor;
    private Button btnCancelarViaje;
    private ProveedoresGeofire proveedoresGeofire;
    private String mExtraOrigin;
    private String mExtraDestination;
    private double extraOrigenLat;
    private double extraOrigenLng;
    private double extraDestinoLat;
    private double extraDestinoLng;
    private LatLng origenLatLng;
    private LatLng destinoLatLng;
    private double radio = 0.1;
    private boolean conductorEncontrado = false;
    private String idConductorEncontrado = "";
    private LatLng conductorEncontradoLatLng;
    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private ProveedoresAutenticacion proveedoresAutenticacion;
    private ProveedoresGoogleApi proveedoresGoogleApi;
    private ValueEventListener mListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_conductor_actividad);

        animationView = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        animationView.playAnimation();
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        extraOrigenLat = getIntent().getDoubleExtra("origin_lat", 0);
        extraOrigenLng = getIntent().getDoubleExtra("origin_lng", 0);
        extraDestinoLat = getIntent().getDoubleExtra("destination_lat", 0);
        extraDestinoLng = getIntent().getDoubleExtra("destination_lng", 0);
        origenLatLng = new LatLng(extraOrigenLat, extraOrigenLng);
        destinoLatLng = new LatLng(extraDestinoLat, extraDestinoLng);

        proveedoresGeofire = new ProveedoresGeofire("Conductores_Activos");
        proveedoresGoogleApi = new ProveedoresGoogleApi(SolicitarConductorActividad.this);
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        proveedoresAutenticacion = new ProveedoresAutenticacion();
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
                    createClientBooking();
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

    private void createClientBooking() {

        proveedoresGoogleApi.getDirections(origenLatLng, conductorEncontradoLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaTexto = distance.getString("text");
                    String duracionTexto = duration.getString("text");
                    sendNotification(duracionTexto, distanciaTexto);
                } catch (Exception e){
                    Log.d("Error", "Mensaje de error:" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void sendNotification(final String tiempo, final String distancia) {
        mTokenProvider.getToken(idConductorEncontrado).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("titulo", "Solicitud de servicio a " + tiempo + " de tu posicion");
                    map.put("contenido", "Un cliente esta solicitando un servicio a una distancia de " + distancia + "\n" +
                            "Origen: " + mExtraOrigin + "\n" +
                            "Destino: " + mExtraDestination);

                    map.put("idClient", proveedoresAutenticacion.getId());
                    FCMBody fcmBody = new FCMBody(token, "high", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    ClientBooking clientBooking = new ClientBooking(
                                            proveedoresAutenticacion.getId(),
                                            idConductorEncontrado,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            tiempo,
                                            distancia,
                                            "create",
                                            extraOrigenLat,
                                            extraOrigenLng,
                                            extraDestinoLat,
                                            extraDestinoLng
                                    );

                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {

                                        public void onSuccess(Void unused) {
                                            checkStatusClientBooking();
                                            //Toast.makeText(SolicitarConductorActividad.this, "La peticion se creo correctamente", Toast.LENGTH_SHORT).show();
                                        }

                                    });
                                    //Toast.makeText(SolicitarConductorActividad.this, "La notificacion se ha enviado correctamente.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(SolicitarConductorActividad.this, "No se pudo enviar la notificacion.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(SolicitarConductorActividad.this, "No se pudo enviar la notificacion.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error" + t.getMessage());
                        }
                    });
                }
                else {
                    Toast.makeText(SolicitarConductorActividad.this, "No se pudo enviar la notificacion.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatusClientBooking() {
        mListener = mClientBookingProvider.getStatus(proveedoresAutenticacion.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("accept")) {
                        Intent intent = new Intent(SolicitarConductorActividad.this,  MapClientBookingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if (status.equals("cancel")) {
                        Toast.makeText(SolicitarConductorActividad.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SolicitarConductorActividad.this, MapaClienteActividad.class);
                        startActivity(intent);
                        finish();
                    }
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
        if (mListener != null) {
            mClientBookingProvider.getStatus(proveedoresAutenticacion.getId()).removeEventListener(mListener);
        }
    }
}