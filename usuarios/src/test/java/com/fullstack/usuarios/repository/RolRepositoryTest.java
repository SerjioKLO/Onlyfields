package com.fullstack.usuarios.repository;

import com.fullstack.usuarios.model.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    @Test
    @DisplayName("Debe guardar y recuperar un rol exitosamente")
    void guardarYRecuperarRol_Exito() {
        // Arrange
        Rol nuevoRol = Rol.builder()
                .nombre("SUPER_ADMIN")
                .build();

        // Act
        Rol rolGuardado = rolRepository.save(nuevoRol);
        Optional<Rol> rolRecuperado = rolRepository.findById(rolGuardado.getId());

        // Assert
        assertThat(rolGuardado).isNotNull();
        assertThat(rolGuardado.getId()).isPositive();
        assertThat(rolRecuperado).isPresent();
        assertThat(rolRecuperado.get().getNombre()).isEqualTo("SUPER_ADMIN");
    }

    @Test
    @DisplayName("Debe encontrar un rol por su nombre")
    void findByNombre_Exito() {
        // Arrange
        Rol rolExistente = Rol.builder()
                .nombre("TESTER")
                .build();
        rolRepository.save(rolExistente);

        // Act
        Optional<Rol> rolEncontrado = rolRepository.findByNombre("TESTER");

        // Assert
        assertThat(rolEncontrado).isPresent();
        assertThat(rolEncontrado.get().getNombre()).isEqualTo("TESTER");
    }

    @Test
    @DisplayName("No debe encontrar un rol si el nombre no existe")
    void findByNombre_NoExiste_RetornaEmpty() {
        // Act
        Optional<Rol> rolEncontrado = rolRepository.findByNombre("ROL_INEXISTENTE");

        // Assert
        assertThat(rolEncontrado).isNotPresent();
    }

    @Test
    @DisplayName("Debe actualizar un rol existente")
    void actualizarRol_Exito() {
        // Arrange
        Rol rol = rolRepository.save(Rol.builder().nombre("OLD_NAME").build());
        
        // Act
        Rol rolParaActualizar = rolRepository.findById(rol.getId()).get();
        rolParaActualizar.setNombre("NEW_NAME");
        rolRepository.save(rolParaActualizar);
        
        Optional<Rol> rolActualizado = rolRepository.findById(rol.getId());

        // Assert
        assertThat(rolActualizado).isPresent();
        assertThat(rolActualizado.get().getNombre()).isEqualTo("NEW_NAME");
    }

    @Test
    @DisplayName("Debe eliminar un rol exitosamente")
    void eliminarRol_Exito() {
        // Arrange
        Rol rol = rolRepository.save(Rol.builder().nombre("TO_DELETE").build());
        Long rolId = rol.getId();

        // Act
        rolRepository.deleteById(rolId);
        Optional<Rol> rolEliminado = rolRepository.findById(rolId);

        // Assert
        assertThat(rolEliminado).isNotPresent();
    }
}
