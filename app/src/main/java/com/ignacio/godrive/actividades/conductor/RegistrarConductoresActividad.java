package com.ignacio.godrive.actividades.conductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.godrive.R;
import com.ignacio.godrive.modelo.Conductores;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresConductores;
import com.shashank.sony.fancytoastlib.FancyToast;

public class RegistrarConductoresActividad extends AppCompatActivity {

    ProveedoresAutenticacion proveedoresAutenticacion;
    ProveedoresConductores proveedoresConductores;

    Button registrar;
    TextInputEditText inputEmail;
    TextInputEditText inputNombre;
    TextInputEditText inputContrasenia;
    TextInputEditText inputMarca;
    TextInputEditText inputPatente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_conductores_actividad);

        proveedoresAutenticacion = new ProveedoresAutenticacion();
        proveedoresConductores = new ProveedoresConductores();


        registrar = findViewById(R.id.btnRegistrar);
        inputEmail = findViewById(R.id.txtInputEmail);
        inputNombre = findViewById(R.id.txtInputNombre);
        inputContrasenia = findViewById(R.id.txtInputContrasenia);
        inputMarca = findViewById(R.id.txtInputMarca);
        inputPatente = findViewById(R.id.txtInputPatente);

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegistrarse();
            }
        });
    }

    void clickRegistrarse(){
        final String email = inputEmail.getText().toString();
        final String nombre = inputNombre.getText().toString();
        final String contrasenia = inputContrasenia.getText().toString();
        final String marca = inputMarca.getText().toString();
        final String patente = inputPatente.getText().toString();

        if (!email.isEmpty() && !nombre.isEmpty() && !contrasenia.isEmpty() && !marca.isEmpty() && !patente.isEmpty()){
            if (contrasenia.length() >= 6){
                registrarse(nombre, email, contrasenia, marca, patente);
            }
            else {
                FancyToast.makeText(RegistrarConductoresActividad.this, "Mínimo 6 caracteres", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            }
        }
        else {
            FancyToast.makeText(RegistrarConductoresActividad.this, "Faltan campos por rellenar", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
        }
    }

    void registrarse(final String nombre, String email, String contrasenia, String marca, String patente){
        proveedoresAutenticacion.registrarse(email, contrasenia).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Conductores conductores= new Conductores(id, nombre, email, marca, patente);
                    Crear(conductores);
                    FancyToast.makeText(RegistrarConductoresActividad.this, "Su registro fue exitoso", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                }
                else {
                    FancyToast.makeText(RegistrarConductoresActividad.this, "No se pudo registrar correctamente", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }
        });
    }

    void Crear(Conductores conductores){
        proveedoresConductores.create(conductores).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //FancyToast.makeText(RegistrarConductoresActividad.this, "El registro fue exitoso", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                    Intent intent = new Intent(RegistrarConductoresActividad.this, MapaConductorActividad.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    FancyToast.makeText(RegistrarConductoresActividad.this, "No se pudo crear el usuario", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }
        });
    }
}