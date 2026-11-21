package com.induztek.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private String contrasenaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Lob
    private byte[] foto;

    private String preguntaSeguridad;
    private String respuestaSeguridadHash;
    private String secCertificacion;
    private String especialidadTecnica;
    private String rutColegiatura;
    private String registroProfesional;
    private String departamentoSucursal;
    private String codigoAutorizacion;
    private String empresaNombre;
    private String rutEmpresa;
    private String direccionEmpresa;
    private String telefonoContacto;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
    public String getPreguntaSeguridad() { return preguntaSeguridad; }
    public void setPreguntaSeguridad(String preguntaSeguridad) { this.preguntaSeguridad = preguntaSeguridad; }
    public String getRespuestaSeguridadHash() { return respuestaSeguridadHash; }
    public void setRespuestaSeguridadHash(String respuestaSeguridadHash) { this.respuestaSeguridadHash = respuestaSeguridadHash; }
    public String getSecCertificacion() { return secCertificacion; }
    public void setSecCertificacion(String secCertificacion) { this.secCertificacion = secCertificacion; }
    public String getEspecialidadTecnica() { return especialidadTecnica; }
    public void setEspecialidadTecnica(String especialidadTecnica) { this.especialidadTecnica = especialidadTecnica; }
    public String getRutColegiatura() { return rutColegiatura; }
    public void setRutColegiatura(String rutColegiatura) { this.rutColegiatura = rutColegiatura; }
    public String getRegistroProfesional() { return registroProfesional; }
    public void setRegistroProfesional(String registroProfesional) { this.registroProfesional = registroProfesional; }
    public String getDepartamentoSucursal() { return departamentoSucursal; }
    public void setDepartamentoSucursal(String departamentoSucursal) { this.departamentoSucursal = departamentoSucursal; }
    public String getCodigoAutorizacion() { return codigoAutorizacion; }
    public void setCodigoAutorizacion(String codigoAutorizacion) { this.codigoAutorizacion = codigoAutorizacion; }
    public String getEmpresaNombre() { return empresaNombre; }
    public void setEmpresaNombre(String empresaNombre) { this.empresaNombre = empresaNombre; }
    public String getRutEmpresa() { return rutEmpresa; }
    public void setRutEmpresa(String rutEmpresa) { this.rutEmpresa = rutEmpresa; }
    public String getDireccionEmpresa() { return direccionEmpresa; }
    public void setDireccionEmpresa(String direccionEmpresa) { this.direccionEmpresa = direccionEmpresa; }
    public String getTelefonoContacto() { return telefonoContacto; }
    public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
}
