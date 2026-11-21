package com.induztek.usuarios.service;

import com.induztek.usuarios.dto.CambiarContrasenaRequest;
import com.induztek.usuarios.dto.LoginRequest;
import com.induztek.usuarios.dto.LoginResponse;
import com.induztek.usuarios.dto.PerfilRequest;
import com.induztek.usuarios.dto.RecuperarRequest;
import com.induztek.usuarios.dto.RegistroRequest;
import com.induztek.usuarios.dto.UsuarioResponse;
import com.induztek.usuarios.exception.ApiException;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.UsuarioRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest r) {
        String username = normalizar(r.username());
        if (repositorio.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        validarContrasena(r.contrasena());
        validarCampoRol(r);
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(r.nombre().trim());
        usuario.setContrasenaHash(codificador.encode(r.contrasena()));
        usuario.setRol(r.rol());
        usuario.setPreguntaSeguridad(r.preguntaSeguridad().trim());
        usuario.setRespuestaSeguridadHash(codificador.encode(normalizar(r.respuestaSeguridad())));
        copiarDatosRol(usuario, r.foto(), r.secCertificacion(), r.especialidadTecnica(), r.rutColegiatura(),
                r.registroProfesional(), r.departamentoSucursal(), r.codigoAutorizacion(), r.empresaNombre(),
                r.rutEmpresa(), r.direccionEmpresa(), r.telefonoContacto());
        return aRespuesta(repositorio.save(usuario));
    }

    public LoginResponse login(LoginRequest r) {
        Usuario usuario = repositorio.findByUsername(normalizar(r.username()))
                .filter(u -> codificador.matches(r.contrasena(), u.getContrasenaHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        return new LoginResponse(usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getRol(),
                UUID.randomUUID().toString());
    }

    @Transactional
    public void recuperarContrasena(RecuperarRequest r) {
        Usuario usuario = repositorio.findByUsername(normalizar(r.username()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (!codificador.matches(normalizar(r.respuestaSeguridad()), usuario.getRespuestaSeguridadHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La respuesta de seguridad es incorrecta");
        }
        validarContrasena(r.nuevaContrasena());
        usuario.setContrasenaHash(codificador.encode(r.nuevaContrasena()));
        repositorio.save(usuario);
    }

    @Transactional
    public void cambiarContrasena(Long id, CambiarContrasenaRequest r) {
        Usuario usuario = buscar(id);
        if (!codificador.matches(r.contrasenaActual(), usuario.getContrasenaHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        validarContrasena(r.nuevaContrasena());
        usuario.setContrasenaHash(codificador.encode(r.nuevaContrasena()));
        repositorio.save(usuario);
    }

    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long id, PerfilRequest r) {
        Usuario usuario = buscar(id);
        usuario.setNombre(r.nombre().trim());
        copiarDatosRol(usuario, r.foto(), r.secCertificacion(), r.especialidadTecnica(), r.rutColegiatura(),
                r.registroProfesional(), r.departamentoSucursal(), r.codigoAutorizacion(), r.empresaNombre(),
                r.rutEmpresa(), r.direccionEmpresa(), r.telefonoContacto());
        return aRespuesta(repositorio.save(usuario));
    }

    public List<UsuarioResponse> listarPorRol(Rol rol) {
        List<Usuario> usuarios = rol == null ? repositorio.findAll() : repositorio.findByRol(rol);
        return usuarios.stream().map(this::aRespuesta).toList();
    }

    private Usuario buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private String normalizar(String texto) {
        return texto.trim().toLowerCase();
    }

    private void validarContrasena(String clave) {
        String error = ValidadorContrasena.validar(clave);
        if (error != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, error);
        }
    }

    private void validarCampoRol(RegistroRequest r) {
        String faltante = switch (r.rol()) {
            case TECNICO -> vacio(r.secCertificacion()) ? "Indica tu certificación SEC" : null;
            case INGENIERO -> vacio(r.rutColegiatura()) ? "Indica tu RUT o N° de colegiatura" : null;
            case JEFE -> vacio(r.departamentoSucursal()) ? "Indica tu departamento o sucursal" : null;
            case CLIENTE -> vacio(r.empresaNombre()) ? "Indica el nombre de la empresa" : null;
        };
        if (faltante != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, faltante);
        }
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private void copiarDatosRol(Usuario u, byte[] foto, String secCertificacion, String especialidadTecnica,
                                String rutColegiatura, String registroProfesional, String departamentoSucursal,
                                String codigoAutorizacion, String empresaNombre, String rutEmpresa,
                                String direccionEmpresa, String telefonoContacto) {
        u.setFoto(foto);
        u.setSecCertificacion(secCertificacion);
        u.setEspecialidadTecnica(especialidadTecnica);
        u.setRutColegiatura(rutColegiatura);
        u.setRegistroProfesional(registroProfesional);
        u.setDepartamentoSucursal(departamentoSucursal);
        u.setCodigoAutorizacion(codigoAutorizacion);
        u.setEmpresaNombre(empresaNombre);
        u.setRutEmpresa(rutEmpresa);
        u.setDireccionEmpresa(direccionEmpresa);
        u.setTelefonoContacto(telefonoContacto);
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getNombre(), u.getRol(), u.getFoto(),
                u.getPreguntaSeguridad(), u.getSecCertificacion(), u.getEspecialidadTecnica(), u.getRutColegiatura(),
                u.getRegistroProfesional(), u.getDepartamentoSucursal(), u.getCodigoAutorizacion(),
                u.getEmpresaNombre(), u.getRutEmpresa(), u.getDireccionEmpresa(), u.getTelefonoContacto());
    }
}
