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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioMock;
    private Rol rolMock;
    private UsuarioRegistroDTO registroDTO;
    private AuthLoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        rolMock = Rol.builder()
                .id(1L)
                .nombre("USER")
                .build();

        usuarioMock = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .email("juan@test.com")
                .passwordHash("hashed-password")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .roles(Set.of(rolMock))
                .build();

        registroDTO = UsuarioRegistroDTO.builder()
                .nombre("Juan Perez")
                .correoElectronico("juan@test.com")
                .password("password")
                .rolId(1L)
                .build();

        loginDTO = AuthLoginDTO.builder()
                .correoElectronico("juan@test.com")
                .password("password")
                .build();
    }

    // ==========================================
    // TESTS PARA: registrarUsuario()
    // ==========================================

    @Test
    @DisplayName("Debe registrar usuario exitosamente")
    void registrarUsuario_Exito() {
        when(usuarioRepository.existsByEmail(registroDTO.getCorreoElectronico())).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolMock));
        when(passwordEncoder.encode(registroDTO.getPassword())).thenReturn("hashed-password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        UsuarioRespuestaDTO resultado = usuarioService.registrarUsuario(registroDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCorreoElectronico()).isEqualTo("juan@test.com");
        assertThat(resultado.getRolNombre()).isEqualTo("USER");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email ya está registrado")
    void registrarUsuario_EmailExistente_LanzaExcepcion() {
        when(usuarioRepository.existsByEmail(registroDTO.getCorreoElectronico())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrarUsuario(registroDTO))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining("ya está registrado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el rol no existe")
    void registrarUsuario_RolNoExiste_LanzaExcepcion() {
        when(usuarioRepository.existsByEmail(registroDTO.getCorreoElectronico())).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.registrarUsuario(registroDTO))
                .isInstanceOf(RolNoEncontradoException.class)
                .hasMessageContaining("no es válido");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==========================================
    // TESTS PARA: login()
    // ==========================================

    @Test
    @DisplayName("Debe hacer login exitosamente")
    void login_Exito() {
        when(usuarioRepository.findByEmail(loginDTO.getCorreoElectronico())).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(loginDTO.getPassword(), usuarioMock.getPasswordHash())).thenReturn(true);

        UsuarioRespuestaDTO resultado = usuarioService.login(loginDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCorreoElectronico()).isEqualTo("juan@test.com");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    void login_UsuarioNoExiste_LanzaExcepcion() {
        when(usuarioRepository.findByEmail(loginDTO.getCorreoElectronico())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(loginDTO))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessageContaining("No se encontró");
    }

    @Test
    @DisplayName("Debe lanzar excepción si la contraseña es incorrecta")
    void login_PasswordIncorrecto_LanzaExcepcion() {
        when(usuarioRepository.findByEmail(loginDTO.getCorreoElectronico())).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(loginDTO.getPassword(), usuarioMock.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(loginDTO))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("incorrecta");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario está inactivo")
    void login_UsuarioInactivo_LanzaExcepcion() {
        usuarioMock.setEstado("INACTIVO");
        when(usuarioRepository.findByEmail(loginDTO.getCorreoElectronico())).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches(loginDTO.getPassword(), usuarioMock.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.login(loginDTO))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessageContaining("no está activa");
    }

    // ==========================================
    // TESTS PARA: listarTodos()
    // ==========================================

    @Test
    @DisplayName("Debe listar todos los usuarios")
    void listarTodos_Exito() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioMock));

        List<UsuarioRespuestaDTO> resultado = usuarioService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCorreoElectronico()).isEqualTo("juan@test.com");
    }

    // ==========================================
    // TESTS PARA: obtenerPorId()
    // ==========================================

    @Test
    @DisplayName("Debe obtener usuario por ID exitosamente")
    void obtenerPorId_Exito() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

        UsuarioRespuestaDTO resultado = usuarioService.obtenerPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe por ID")
    void obtenerPorId_NoExiste_LanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(1L))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessageContaining("no encontrado con el ID");
    }

    // ==========================================
    // TESTS PARA: eliminarUsuario()
    // ==========================================

    @Test
    @DisplayName("Debe eliminar (desactivar) usuario exitosamente")
    void eliminarUsuario_Exito() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        usuarioService.eliminarUsuario(1L);

        assertThat(usuarioMock.getEstado()).isEqualTo("INACTIVO");
        verify(usuarioRepository).save(usuarioMock);
    }
}