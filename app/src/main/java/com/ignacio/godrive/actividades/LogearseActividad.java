package com.ignacio.godrive.actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ignacio.godrive.R;
import com.ignacio.godrive.actividades.cliente.MapaClienteActividad;
import com.ignacio.godrive.actividades.cliente.RegistrarActividad;
import com.ignacio.godrive.actividades.conductor.MapaConductorActividad;
import com.ignacio.godrive.actividades.conductor.RegistrarConductoresActividad;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LogearseActividad extends AppCompatActivity {

    // Declaración de variables para los botones y input
    TextInputEditText email;
    TextInputEditText contrasenia;
    Button loguearse;
    Button registrarse;
    SharedPreferences preferencias;
    View view;

    FirebaseAuth autenticacion; // Autenticación de Firebase
    DatabaseReference basedatos; // Referencia a la base de datos de Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_logearse);

        // Inicialización los input y botones mediante sus IDs
        email = findViewById(R.id.txtEmail);
        contrasenia = findViewById(R.id.txtContrasenia);
        registrarse = findViewById(R.id.btnRegistrar);
        view = findViewById(R.id.myProgressButton);
        final ProgressButton progressButton = new ProgressButton(LogearseActividad.this, view); // Crear ProgressButton

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressButton.botonActivado(); // Activar el botón
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        iniciarSesion(progressButton); // Pasar el ProgressButton como argumento
                    }
                }, 3000);
            }
        });

        preferencias = getApplicationContext().getSharedPreferences("tipoUsuario", MODE_PRIVATE);

        // Inicialización de la autenticación de Firebase
        autenticacion = FirebaseAuth.getInstance();

        // Inicialización de la referencia a la base de datos de Firebase
        basedatos = FirebaseDatabase.getInstance().getReference();

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pantallaRegistrarse();
            }
        });
    }

    private void pantallaRegistrarse() {
        String tipoUsuario = preferencias.getString("tipoUsuario", "");

        if ("Cliente".equals(tipoUsuario)) {
            Intent pantallaRegistroCliente = new Intent(LogearseActividad.this, RegistrarActividad.class);
            startActivity(pantallaRegistroCliente);
        } else if ("Conductor".equals(tipoUsuario)) {
            Intent pantallaRegistroConductor = new Intent(LogearseActividad.this, RegistrarConductoresActividad.class);
            startActivity(pantallaRegistroConductor);
        } else {
            // Manejar caso de tipo de usuario no válido
            FancyToast.makeText(LogearseActividad.this, "Seleccione un tipo de usuario válido", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
        }
    }

    // Método para iniciar sesión
    private void iniciarSesion(final ProgressButton progressButton) {
        String obtenerEmail = email.getText().toString();
        String obtenerPassword = contrasenia.getText().toString();

        // Verificar si los campos de email y contraseña no están vacíos
        if (!obtenerEmail.isEmpty() && !obtenerPassword.isEmpty()) {
            if (obtenerPassword.length() >= 6) { // Verificar que la contraseña tenga al menos 6 caracteres
                autenticacion.signInWithEmailAndPassword(obtenerEmail, obtenerPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String user = preferencias.getString("tipoUsuario", "");
                            if (user.equals("Cliente")) {
                                Handler handler1 = new Handler();
                                handler1.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LogearseActividad.this, MapaClienteActividad.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }, 0);
                            } else {
                                Handler handler1 = new Handler();
                                handler1.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(LogearseActividad.this, MapaConductorActividad.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }, 0);
                            }
                        } else {
                            Handler handler4 = new Handler();
                            handler4.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    FancyToast.makeText(LogearseActividad.this, "La contraseña o el email son incorrectos", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                }
                            }, 1000);
                            progressButton.botonFallado(); // Cambiar a "Iniciar Sesión" si las credenciales son incorrectas
                        }
                    }
                });
            } else {
                Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FancyToast.makeText(LogearseActividad.this, "La contraseña debe tener al menos 6 caracteres", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    }
                }, 1000);
                progressButton.botonFallado(); // Cambiar a "Iniciar Sesión" si las credenciales son incorrectas
            }
        } else {
            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FancyToast.makeText(LogearseActividad.this, "Faltan campos por rellenar", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
            }, 1000);
            progressButton.botonFallado(); // Cambiar a "Iniciar Sesión" si las credenciales son incorrectas
        }
    }
}