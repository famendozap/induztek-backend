package com.induztek.protocolos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protocolos")
public class Protocolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long equipoId;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProtocolo estado;

    @Column(length = 2000)
    private String observaciones;

    @Lob
    private byte[] fotoEvidenciaBytes;

    private String tecnicoNombre;
    private String ingenieroNombre;
    private Double temperaturaAmbiente;
    private Double humedadRelativa;
    private String instrumentosUsados;
    private String resultadoGeneral;
    private String ubicacionGPS;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "protocolo_mediciones", joinColumns = @JoinColumn(name = "protocolo_id"))
    private List<Medicion> mediciones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEquipoId() { return equipoId; }
    public void setEquipoId(Long equipoId) { this.equipoId = equipoId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public EstadoProtocolo getEstado() { return estado; }
    public void setEstado(EstadoProtocolo estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public byte[] getFotoEvidenciaBytes() { return fotoEvidenciaBytes; }
    public void setFotoEvidenciaBytes(byte[] fotoEvidenciaBytes) { this.fotoEvidenciaBytes = fotoEvidenciaBytes; }
    public String getTecnicoNombre() { return tecnicoNombre; }
    public void setTecnicoNombre(String tecnicoNombre) { this.tecnicoNombre = tecnicoNombre; }
    public String getIngenieroNombre() { return ingenieroNombre; }
    public void setIngenieroNombre(String ingenieroNombre) { this.ingenieroNombre = ingenieroNombre; }
    public Double getTemperaturaAmbiente() { return temperaturaAmbiente; }
    public void setTemperaturaAmbiente(Double temperaturaAmbiente) { this.temperaturaAmbiente = temperaturaAmbiente; }
    public Double getHumedadRelativa() { return humedadRelativa; }
    public void setHumedadRelativa(Double humedadRelativa) { this.humedadRelativa = humedadRelativa; }
    public String getInstrumentosUsados() { return instrumentosUsados; }
    public void setInstrumentosUsados(String instrumentosUsados) { this.instrumentosUsados = instrumentosUsados; }
    public String getResultadoGeneral() { return resultadoGeneral; }
    public void setResultadoGeneral(String resultadoGeneral) { this.resultadoGeneral = resultadoGeneral; }
    public String getUbicacionGPS() { return ubicacionGPS; }
    public void setUbicacionGPS(String ubicacionGPS) { this.ubicacionGPS = ubicacionGPS; }
    public List<Medicion> getMediciones() { return mediciones; }
    public void setMediciones(List<Medicion> mediciones) { this.mediciones = mediciones; }
}
