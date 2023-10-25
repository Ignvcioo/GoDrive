package com.ignacio.godrive.actividades;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ignacio.godrive.R;

public class ProgressButton {

    View logearseActividad;
    private CardView cardView;
    private ProgressBar progressBar;
    private TextView textView;
    private ConstraintLayout layout;
    private Animation rotationAnimation;

    ProgressButton (Context contexto, View view){
        cardView = view.findViewById(R.id.cardView);
        layout = view.findViewById(R.id.constraint_layout);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.textView);
        rotationAnimation = AnimationUtils.loadAnimation(contexto, R.anim.rotate);
    }

    void botonActivado(){
        progressBar.startAnimation(rotationAnimation);
        progressBar.setVisibility(View.VISIBLE);
        textView.setText("Por favor, espere...");
    }

    void botonFallado(){
        layout.setBackgroundColor(cardView.getResources().getColor(R.color.colorPrimary));
        progressBar.setVisibility(View.GONE);
        textView.setText("Iniciar Sesión");
        progressBar.clearAnimation();
    }
}
