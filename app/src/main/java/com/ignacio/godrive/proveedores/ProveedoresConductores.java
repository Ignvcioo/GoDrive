package com.ignacio.godrive.proveedores;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ignacio.godrive.modelo.Conductores;

// Clase que proporciona métodos para interactuar con la base de datos Firebase Realtime Database y gestionar la información de conductores.
public class ProveedoresConductores {

    // Referencia a la base de datos Firebase Realtime Database.
    DatabaseReference baseDatos;

    // Constructor de la clase. Inicializa la referencia a la ubicación "Usuarios/Conductores" en la base de datos.
    public ProveedoresConductores(){
        baseDatos = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Conductores");
    }
    // Método para crear un nuevo registro de conductor en la base de datos.
    public Task<Void> create(Conductores conductores){
        // Crea un nuevo registro de conductor en la base de datos o actualiza uno existente si el ID ya existe.
        return baseDatos.child(conductores.getId()).setValue(conductores);
    }
}
