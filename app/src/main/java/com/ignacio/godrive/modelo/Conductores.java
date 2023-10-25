package com.ignacio.godrive.modelo;

public class Conductores {

    // Atributos
    String id;
    String nombre;
    String email;
    String vehiculo;
    String patente;

    // Constructor
    public Conductores(String id, String nombre, String email, String vehiculo, String patente) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.vehiculo = vehiculo;
        this.patente = patente;
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

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }
}
