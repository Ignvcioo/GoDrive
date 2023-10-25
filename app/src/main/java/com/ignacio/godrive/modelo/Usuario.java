package com.ignacio.godrive.modelo;
public class Usuario {

    // Atributos
    String id;
    String nombre;
    String email;

    public Usuario() {
    }

    // Constructor
    public Usuario(String id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    // Getter y Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
