package com.induztek.protocolos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Medicion {

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "unidad")
    private String unidad;

    public Medicion() {
    }

    public Medicion(String nombre, String valor, String unidad) {
        this.nombre = nombre;
        this.valor = valor;
        this.unidad = unidad;
    }

    public String getNombre() { return nombre; }
    public String getValor() { return valor; }
    public String getUnidad() { return unidad; }
}
