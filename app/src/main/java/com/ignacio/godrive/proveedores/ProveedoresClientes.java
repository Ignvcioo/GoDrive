package com.ignacio.godrive.proveedores;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ignacio.godrive.modelo.Clientes;
import java.util.HashMap;
import java.util.Map;

// Clase que proporciona métodos para interactuar con la base de datos Firebase Realtime Database y gestionar la información de clientes.
public class ProveedoresClientes {

    // Referencia a la base de datos Firebase Realtime Database.
    DatabaseReference baseDatos;

    // Constructor de la clase. Inicializa la referencia a la ubicación "Usuarios/Clientes" en la base de datos.
    public ProveedoresClientes(){
        baseDatos = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Clientes");
    }

    // Método para crear un nuevo registro de cliente en la base de datos.
    public Task<Void> create(Clientes clientes){
        // Se crea un mapa (HashMap) para almacenar los datos del cliente.
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", clientes.getNombre());
        map.put("email", clientes.getEmail());
        //Crea un nuevo registro de cliente en la base de datos o actualiza uno existente si el ID ya existe.
        return baseDatos.child(clientes.getId()).setValue(map);
    }
}
