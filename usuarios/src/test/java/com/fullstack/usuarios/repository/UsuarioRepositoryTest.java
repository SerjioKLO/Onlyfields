package com.fullstack.usuarios.repository;

import com.fullstack.usuarios.model.Rol;
import com.fullstack.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Rol rolUser;

    @BeforeEach
    void setUp() {
        rolUser = entityManager.persist(Rol.builder().nombre("USER").build());
    }

    private Usuario createAndPersistUsuario(String email, String nombre) {
        Set<Rol> roles = new HashSet<>();
        roles.add(rolUser);

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .passwordHash("password")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .roles(roles) // Usar un Set mutable
                .build();
        return entityManager.persistAndFlush(usuario);
    }

    @Test
    @DisplayName("Debe guardar y recuperar un usuario exitosamente")
    void guardarYRecuperarUsuario_Exito() {
        Usuario nuevoUsuario = createAndPersistUsuario("test@test.com", "Test User");

        Optional<Usuario> usuarioRecuperado = usuarioRepository.findById(nuevoUsuario.getId());

        assertThat(usuarioRecuperado).isPresent();
        assertThat(usuarioRecuperado.get().getEmail()).isEqualTo("test@test.com");
        assertThat(usuarioRecuperado.get().getRoles()).contains(rolUser);
    }

    @Test
    @DisplayName("Debe encontrar un usuario por su email")
    void findByEmail_Exito() {
        createAndPersistUsuario("findme@test.com", "Find Me");

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail("findme@test.com");

        assertThat(usuarioEncontrado).isPresent();
        assertThat(usuarioEncontrado.get().getNombre()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("No debe encontrar un usuario si el email no existe")
    void findByEmail_NoExiste_RetornaEmpty() {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByEmail("nonexistent@test.com");

        assertThat(usuarioEncontrado).isNotPresent();
    }

    @Test
    @DisplayName("Debe retornar true si el email existe")
    void existsByEmail_Existe_RetornaTrue() {
        createAndPersistUsuario("exists@test.com", "Exists");

        boolean existe = usuarioRepository.existsByEmail("exists@test.com");

        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false si el email no existe")
    void existsByEmail_NoExiste_RetornaFalse() {
        boolean existe = usuarioRepository.existsByEmail("nonexistent@test.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("Debe actualizar un usuario existente")
    void actualizarUsuario_Exito() {
        Usuario usuario = createAndPersistUsuario("update@test.com", "Old Name");
        
        Usuario usuarioParaActualizar = usuarioRepository.findById(usuario.getId()).get();
        usuarioParaActualizar.setNombre("New Name");
        usuarioRepository.save(usuarioParaActualizar);
        
        Optional<Usuario> usuarioActualizado = usuarioRepository.findById(usuario.getId());

        assertThat(usuarioActualizado).isPresent();
        assertThat(usuarioActualizado.get().getNombre()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("Debe eliminar un usuario exitosamente")
    void eliminarUsuario_Exito() {
        Usuario usuario = createAndPersistUsuario("delete@test.com", "To Delete");
        Long usuarioId = usuario.getId();

        usuarioRepository.deleteById(usuarioId);
        Optional<Usuario> usuarioEliminado = usuarioRepository.findById(usuarioId);

        assertThat(usuarioEliminado).isNotPresent();
    }
}
