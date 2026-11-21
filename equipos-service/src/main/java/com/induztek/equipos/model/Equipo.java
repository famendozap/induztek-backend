package com.induztek.equipos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "equipos", uniqueConstraints = @UniqueConstraint(columnNames = {"instalacion_cliente", "codigo"}))
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEquipo tipo;

    @Column(name = "instalacion_cliente", nullable = false)
    private String instalacionCliente;

    private String marca;
    private String modelo;
    private String numeroSerie;
    private Double potenciaKVA;
    private Double tensionNominalKV;
    private Long clienteId;
    private String clienteNombre;
    private String ubicacionGPS;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoEquipo getTipo() { return tipo; }
    public void setTipo(TipoEquipo tipo) { this.tipo = tipo; }
    public String getInstalacionCliente() { return instalacionCliente; }
    public void setInstalacionCliente(String instalacionCliente) { this.instalacionCliente = instalacionCliente; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public Double getPotenciaKVA() { return potenciaKVA; }
    public void setPotenciaKVA(Double potenciaKVA) { this.potenciaKVA = potenciaKVA; }
    public Double getTensionNominalKV() { return tensionNominalKV; }
    public void setTensionNominalKV(Double tensionNominalKV) { this.tensionNominalKV = tensionNominalKV; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getUbicacionGPS() { return ubicacionGPS; }
    public void setUbicacionGPS(String ubicacionGPS) { this.ubicacionGPS = ubicacionGPS; }
}
