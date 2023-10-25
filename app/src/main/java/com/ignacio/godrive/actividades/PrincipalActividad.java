package com.ignacio.godrive.actividades;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.cliente.MapaClienteActividad;
import com.ignacio.godrive.actividades.conductor.MapaConductorActividad;

public class PrincipalActividad extends AppCompatActivity {

    // Declaración de variables para los botones y SharedPreferences
    Button conductor;
    Button cliente;
    SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        // Inicialización de los botones mediante sus IDs
        conductor = findViewById(R.id.btnConductor);
        cliente = findViewById(R.id.btnCliente);

        // Inicialización de SharedPreferences para almacenar el tipo de usuario
        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();

        // Configuración de onClickListener para el botón de "Conductor"
        conductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Al hacer clic en el botón "Conductor", se guarda la elección en SharedPreferences
                editor.putString("usuario", "Conductor");
                editor.putString("tipoUsuario", "Conductor");
                editor.apply(); // Aplicar los cambios al SharedPreferences
                pantallaOpcion(); // Cambiar a la pantalla de selección de opcion
            }
        });

        // Configuración de onClickListener para el botón de "Cliente"
        cliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Al hacer clic en el botón "Cliente", se guarda la elección en SharedPreferences
                editor.putString("usuario", "Cliente");
                editor.putString("tipoUsuario", "Cliente");
                editor.apply();
                pantallaOpcion();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            String user = preferencias.getString("tipoUsuario", "");
            if (user.equals("Cliente")){
                Intent intent = new Intent(PrincipalActividad.this, MapaClienteActividad.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(PrincipalActividad.this, MapaConductorActividad.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    // Método para cambiar a la pantalla de selección de opciones
    private void pantallaOpcion() {
        Intent pantallaOpcion= new Intent(PrincipalActividad.this, LogearseActividad.class);
        startActivity(pantallaOpcion); // Iniciar la nueva actividad
    }
}