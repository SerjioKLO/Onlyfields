package com.fullstack.usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.usuarios.dto.AuthLoginDTO;
import com.fullstack.usuarios.dto.UsuarioRegistroDTO;
import com.fullstack.usuarios.dto.UsuarioRespuestaDTO;
import com.fullstack.usuarios.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsuarioController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe retornar 200 OK al obtener un usuario por ID")
    void obtenerPorId_Retorna200() throws Exception {
        UsuarioRespuestaDTO usuario = UsuarioRespuestaDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .correoElectronico("juan@test.com")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .rolNombre("ADMIN")
                .build();
        when(usuarioService.obtenerPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                .andExpect(jsonPath("$.correoElectronico").value("juan@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 201 Created al registrar un usuario")
    void registrar_Retorna201() throws Exception {
        UsuarioRegistroDTO registroDTO = UsuarioRegistroDTO.builder()
                .nombre("Ana Gomez")
                .correoElectronico("ana@test.com")
                .password("123456789")
                .rolId(2L)
                .build();
        UsuarioRespuestaDTO respuestaDTO = UsuarioRespuestaDTO.builder()
                .id(2L)
                .nombre("Ana Gomez")
                .correoElectronico("ana@test.com")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .rolNombre("USER")
                .build();

        when(usuarioService.registrarUsuario(any(UsuarioRegistroDTO.class))).thenReturn(respuestaDTO);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Ana Gomez"))
                .andExpect(jsonPath("$.correoElectronico").value("ana@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK al hacer login exitoso")
    void login_Retorna200() throws Exception {
        AuthLoginDTO loginDTO = AuthLoginDTO.builder()
                .correoElectronico("juan@test.com")
                .password("password")
                .build();
        UsuarioRespuestaDTO respuestaDTO = UsuarioRespuestaDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .correoElectronico("juan@test.com")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .rolNombre("ADMIN")
                .build();

        when(usuarioService.login(any(AuthLoginDTO.class))).thenReturn(respuestaDTO);

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correoElectronico").value("juan@test.com"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK al listar todos los usuarios")
    void listarTodos_Retorna200() throws Exception {
        UsuarioRespuestaDTO user1 = UsuarioRespuestaDTO.builder()
                .id(1L)
                .nombre("Juan Perez")
                .correoElectronico("juan@test.com")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .rolNombre("ADMIN")
                .build();
        UsuarioRespuestaDTO user2 = UsuarioRespuestaDTO.builder()
                .id(2L)
                .nombre("Ana Gomez")
                .correoElectronico("ana@test.com")
                .estado("ACTIVO")
                .fechaCreacion(LocalDateTime.now())
                .rolNombre("USER")
                .build();

        when(usuarioService.listarTodos()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @DisplayName("Debe retornar 204 No Content al eliminar un usuario")
    void eliminar_Retorna204() throws Exception {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isNoContent());
    }
}
