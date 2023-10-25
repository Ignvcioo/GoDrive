package com.ignacio.godrive.actividades.cliente;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ignacio.godrive.R;
import com.ignacio.godrive.modelo.Clientes;
import com.ignacio.godrive.proveedores.ProveedoresAutenticacion;
import com.ignacio.godrive.proveedores.ProveedoresClientes;
import com.shashank.sony.fancytoastlib.FancyToast;

public class RegistrarActividad extends AppCompatActivity {


    SharedPreferences preferencias;

    ProveedoresAutenticacion proveedoresAutenticacion;
    ProveedoresClientes proveedoresClientes;

    Button registrar;
    TextInputEditText inputEmail;
    TextInputEditText inputNombre;
    TextInputEditText inputContrasenia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_registrar);

        proveedoresAutenticacion = new ProveedoresAutenticacion();
        proveedoresClientes = new ProveedoresClientes();


        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        registrar = findViewById(R.id.btnRegistrar);
        inputEmail = findViewById(R.id.txtInputEmail);
        inputNombre = findViewById(R.id.txtInputNombre);
        inputContrasenia = findViewById(R.id.txtInputContrasenia);

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

        if (!email.isEmpty() && !nombre.isEmpty() && !contrasenia.isEmpty()){
            if (contrasenia.length() >= 6){
                registrarse(nombre, email, contrasenia);
            }
            else {
                FancyToast.makeText(RegistrarActividad.this, "Mínimo 6 caracteres", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
            }
        }
        else {
            FancyToast.makeText(RegistrarActividad.this, "Faltan campos por rellenar", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
        }
    }

    void registrarse(final String nombre, String email, String contrasenia){
        proveedoresAutenticacion.registrarse(email, contrasenia).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Clientes clientes= new Clientes(id, nombre, email);
                    Crear(clientes);
                    FancyToast.makeText(RegistrarActividad.this, "Su registro fue exitoso", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                }
                else {
                    FancyToast.makeText(RegistrarActividad.this, "No se pudo registrar correctamente", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }
        });
    }

    void Crear(Clientes clientes){
        proveedoresClientes.create(clientes).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //FancyToast.makeText(RegistrarActividad.this, "El registro fue exitoso", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                    Intent intent = new Intent(RegistrarActividad.this, MapaClienteActividad.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    FancyToast.makeText(RegistrarActividad.this, "No se pudo crear el usuario", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }
        });
    }
}