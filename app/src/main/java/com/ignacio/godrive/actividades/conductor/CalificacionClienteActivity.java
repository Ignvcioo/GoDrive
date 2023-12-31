package com.ignacio.godrive.actividades.conductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.cliente.CalificationDriverActivity;
import com.ignacio.godrive.actividades.cliente.MapaClienteActividad;
import com.ignacio.godrive.modelo.ClientBooking;
import com.ignacio.godrive.modelo.HistoryBooking;
import com.ignacio.godrive.proveedores.ClientBookingProvider;
import com.ignacio.godrive.proveedores.HistoryBookingProvider;

import java.util.Date;

public class CalificacionClienteActivity extends AppCompatActivity {
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatinBar;
    private Button mButtonCalification;
    private ClientBookingProvider mClientBookingProvider;
    private String mExtraClientId;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private float mCalification = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificacion_cliente);

        mTextViewDestination = findViewById(R.id.textViewDestinationCalification);
        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mRatinBar = findViewById(R.id.ratingbarCalification);
        mButtonCalification = findViewById(R.id.btnCalification);
        mClientBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        mExtraClientId = getIntent().getStringExtra("idClient");

        mRatinBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calification, boolean b) {
                mCalification = calification;
            }
        });

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calificate();
            }
        });
        getClientBooking();
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());
                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificate() {
        if (mCalification > 0) {
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mHistoryBookingProvider.updateCalifictionClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionClienteActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionClienteActivity.this, MapaConductorActividad.class);
                                startActivity(intent);
                                finish();
                            }
                        });;
                    }
                    else {
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionClienteActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificacionClienteActivity.this, MapaConductorActividad.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            Toast.makeText(this, "Debe ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}
