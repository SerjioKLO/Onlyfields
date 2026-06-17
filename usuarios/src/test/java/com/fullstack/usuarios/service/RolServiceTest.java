package com.fullstack.usuarios.service;

import com.fullstack.usuarios.dto.RolDTO;
import com.fullstack.usuarios.exception.EmailYaRegistradoException;
import com.fullstack.usuarios.exception.RolConUsuariosException;
import com.fullstack.usuarios.exception.RolNoEncontradoException;
import com.fullstack.usuarios.model.Rol;
import com.fullstack.usuarios.model.Usuario;
import com.fullstack.usuarios.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolService rolService;

    private Rol rolMock;
    private RolDTO rolDTO;

    @BeforeEach
    void setUp() {
        rolMock = Rol.builder()
                .id(1L)
                .nombre("ADMIN")
                .descripcion("Administrador del sistema")
                .usuarios(Collections.emptySet())
                .build();

        rolDTO = RolDTO.builder()
                .nombre("ADMIN")
                .descripcion("Administrador del sistema")
                .build();
    }

    @Test
    @DisplayName("Debe crear un rol exitosamente")
    void crearRol_Exito() {
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenReturn(rolMock);

        RolDTO resultado = rolService.crearRol(rolDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("ADMIN");
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el rol ya existe al crear")
    void crearRol_RolExistente_LanzaExcepcion() {
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolMock));

        assertThatThrownBy(() -> rolService.crearRol(rolDTO))
                .isInstanceOf(EmailYaRegistradoException.class)
                .hasMessageContaining("ya existe");

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    @DisplayName("Debe listar todos los roles")
    void listarRoles_Exito() {
        when(rolRepository.findAll()).thenReturn(List.of(rolMock));

        List<RolDTO> resultado = rolService.listarRoles();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Debe actualizar un rol exitosamente")
    void actualizarRol_Exito() {
        RolDTO dtoActualizado = RolDTO.builder().nombre("SUPER_ADMIN").descripcion("Nueva desc").build();
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolMock));
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RolDTO resultado = rolService.actualizarRol(1L, dtoActualizado);

        assertThat(resultado.getNombre()).isEqualTo("SUPER_ADMIN");
        assertThat(resultado.getDescripcion()).isEqualTo("Nueva desc");
    }

    @Test
    @DisplayName("Debe lanzar excepción si el rol no existe al actualizar")
    void actualizarRol_NoExiste_LanzaExcepcion() {
        when(rolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.actualizarRol(1L, rolDTO))
                .isInstanceOf(RolNoEncontradoException.class);
    }

    @Test
    @DisplayName("Debe eliminar un rol exitosamente si no tiene usuarios")
    void eliminarRol_SinUsuarios_Exito() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolMock));
        doNothing().when(rolRepository).delete(rolMock);

        rolService.eliminarRol(1L);

        verify(rolRepository).delete(rolMock);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el rol tiene usuarios asignados al eliminar")
    void eliminarRol_ConUsuarios_LanzaExcepcion() {
        rolMock.setUsuarios(Set.of(new Usuario())); // Simular que tiene un usuario
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rolMock));

        assertThatThrownBy(() -> rolService.eliminarRol(1L))
                .isInstanceOf(RolConUsuariosException.class)
                .hasMessageContaining("tiene 1 usuario(s) asignado(s)");

        verify(rolRepository, never()).delete(any(Rol.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el rol no existe al eliminar")
    void eliminarRol_NoExiste_LanzaExcepcion() {
        when(rolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.eliminarRol(1L))
                .isInstanceOf(RolNoEncontradoException.class);
    }
}
