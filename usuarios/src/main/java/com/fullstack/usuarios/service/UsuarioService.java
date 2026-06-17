package com.fullstack.usuarios.service;

import com.fullstack.usuarios.dto.AuthLoginDTO;
import com.fullstack.usuarios.dto.UsuarioRegistroDTO;
import com.fullstack.usuarios.dto.UsuarioRespuestaDTO;
import com.fullstack.usuarios.exception.CredencialesInvalidasException;
import com.fullstack.usuarios.exception.EmailYaRegistradoException;
import com.fullstack.usuarios.exception.RolNoEncontradoException;
import com.fullstack.usuarios.exception.UsuarioNoEncontradoException;
import com.fullstack.usuarios.model.Rol;
import com.fullstack.usuarios.model.Usuario;
import com.fullstack.usuarios.repository.RolRepository;
import com.fullstack.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que encapsula la lógica de negocio para la gestión de usuarios y autenticación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     * @param dto DTO con los datos de registro.
     * @return DTO con la información del usuario creado.
     */
    @Transactional
    public UsuarioRespuestaDTO registrarUsuario(UsuarioRegistroDTO dto) {
        log.info("[ms-usuarios] Iniciando registro de nuevo usuario con email: {}", dto.getCorreoElectronico());

        if (usuarioRepository.existsByEmail(dto.getCorreoElectronico())) {
            log.warn("[ms-usuarios] Fallo de registro: El correo '{}' ya se encuentra en uso.", dto.getCorreoElectronico());
            throw new EmailYaRegistradoException("El correo electrónico '" + dto.getCorreoElectronico() + "' ya está registrado.");
        }

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> {
                    log.warn("[ms-usuarios] Fallo de registro: Rol con ID '{}' no encontrado.", dto.getRolId());
                    return new RolNoEncontradoException("El rol con ID '" + dto.getRolId() + "' no es válido.");
                });

        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .email(dto.getCorreoElectronico())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .build();

        usuario.agregarRol(rol);
        Usuario nuevoUsuario = usuarioRepository.save(usuario);

        log.info("[ms-usuarios] Usuario registrado exitosamente con ID: {}", nuevoUsuario.getId());
        return mapearARespuestaDTO(nuevoUsuario);
    }

    /**
     * Valida las credenciales de un usuario para el login.
     * @param dto DTO con email y contraseña.
     * @return DTO con la información del usuario si el login es exitoso.
     */
    @Transactional(readOnly = true)
    public UsuarioRespuestaDTO login(AuthLoginDTO dto) {
        log.info("[ms-usuarios] Intento de login para el correo: {}", dto.getCorreoElectronico());

        Usuario usuario = usuarioRepository.findByEmail(dto.getCorreoElectronico())
                .orElseThrow(() -> {
                    log.warn("[ms-usuarios] Login fallido: No existe cuenta con el correo '{}'.", dto.getCorreoElectronico());
                    return new UsuarioNoEncontradoException("No se encontró un usuario con el correo: " + dto.getCorreoElectronico());
                });

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            log.warn("[ms-usuarios] Login fallido para '{}': Contraseña incorrecta.", dto.getCorreoElectronico());
            throw new CredencialesInvalidasException("La contraseña es incorrecta.");
        }

        if (!"ACTIVO".equals(usuario.getEstado())) {
            log.warn("[ms-usuarios] Login rechazado para '{}': Cuenta inactiva.", dto.getCorreoElectronico());
            throw new CredencialesInvalidasException("La cuenta del usuario no está activa.");
        }

        log.info("[ms-usuarios] Login exitoso para el usuario ID: {}", usuario.getId());
        return mapearARespuestaDTO(usuario);
    }

    /**
     * Obtiene una lista de todos los usuarios.
     * @return Lista de DTOs de usuario.
     */
    @Transactional(readOnly = true)
    public List<UsuarioRespuestaDTO> listarTodos() {
        log.info("[ms-usuarios] Solicitud para listar todos los usuarios registrados.");
        return usuarioRepository.findAll().stream()
                .map(this::mapearARespuestaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario.
     * @return DTO con la información del usuario.
     */
    @Transactional(readOnly = true)
    public UsuarioRespuestaDTO obtenerPorId(Long id) {
        log.info("[ms-usuarios] Buscando usuario con ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ms-usuarios] Búsqueda fallida: Usuario con ID '{}' no encontrado.", id);
                    return new UsuarioNoEncontradoException("Usuario no encontrado con el ID: " + id);
                });

        return mapearARespuestaDTO(usuario);
    }

    /**
     * Realiza un borrado lógico de un usuario, cambiando su estado a "INACTIVO".
     * @param id El ID del usuario a desactivar.
     */
    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("[ms-usuarios] Solicitud de baja lógica para el usuario ID: {}", id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[ms-usuarios] Baja fallida: Usuario con ID '{}' no existe.", id);
                    return new UsuarioNoEncontradoException("Usuario no encontrado con el ID: " + id);
                });

        usuario.setEstado("INACTIVO");
        usuarioRepository.save(usuario);

        log.info("[ms-usuarios] Usuario ID '{}' dado de baja (INACTIVO) exitosamente.", id);
    }

    /**
     * Método de utilidad para convertir una entidad {@link Usuario} a un {@link UsuarioRespuestaDTO}.
     */
    private UsuarioRespuestaDTO mapearARespuestaDTO(Usuario usuario) {
        String rolNombre = usuario.getRoles().stream()
                .findFirst()
                .map(Rol::getNombre)
                .orElse("SIN_ROL");

        return UsuarioRespuestaDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correoElectronico(usuario.getEmail())
                .estado(usuario.getEstado())
                .fechaCreacion(usuario.getFechaCreacion())
                .rolNombre(rolNombre)
                .build();
    }
}