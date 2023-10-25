package com.ignacio.godrive.proveedores;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// Clase que proporciona métodos para la autenticación de usuarios mediante Firebase Authentication.
public class ProveedoresAutenticacion {
    FirebaseAuth autenticacion;

    // Constructor de la clase. Inicializa la instancia de FirebaseAuth.
    public ProveedoresAutenticacion(){
        autenticacion = FirebaseAuth.getInstance();
    }

    // Método para registrar un nuevo usuario en Firebase Authentication y devuelve una tarea (Task) que representa la operación de registro.
    public Task<AuthResult> registrarse(String email, String contrasenia){
        return autenticacion.createUserWithEmailAndPassword(email, contrasenia);
    }

    // Método para iniciar sesión con un usuario existente en Firebase Authentication y devuelve una tarea (Task) que representa la operación de inicio de sesión.
    public Task<AuthResult> logearse(String email, String contrasenia){
        return autenticacion.signInWithEmailAndPassword(email, contrasenia);
    }

    // Método para cerrar sesión de un usuario autenticado.
    public void cerrarSesion(){
        autenticacion.signOut();
    }

    public String getId(){
        return autenticacion.getCurrentUser().getUid();
    }

    public boolean sesionExistente(){
        boolean existente = false;
        if (autenticacion.getCurrentUser() != null){
            existente = true;
        }
        return existente;
    }
}
