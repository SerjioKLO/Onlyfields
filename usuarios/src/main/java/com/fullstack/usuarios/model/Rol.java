package com.fullstack.usuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Representa un rol o perfil de usuario en el sistema (ej. "ADMIN", "USER").
 * Esta entidad se mapea a la tabla "rol".
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa un rol de usuario en el sistema")
public class Rol {

    /**
     * Identificador único del rol (autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del rol", example = "1")
    private Long id;

    /**
     * Nombre único del rol (ej. "ROLE_ADMIN", "ROLE_USER").
     * Es la clave funcional del rol.
     */
    @Column(unique = true, nullable = false)
    @Schema(description = "Nombre único del rol", example = "ROLE_ADMIN")
    private String nombre;

    /**
     * Descripción legible por humanos de lo que implica el rol.
     */
    @Schema(description = "Descripción del rol", example = "Rol con permisos de administrador")
    private String descripcion;

    /**
     * Conjunto de usuarios que tienen este rol.
     * Es el lado inverso de la relación muchos a muchos definida en la entidad Usuario.
     * 'mappedBy = "roles"' indica que la entidad Usuario es la propietaria de la relación.
     * Se utiliza @JsonIgnore para evitar problemas de serialización infinita al exponer la entidad en un API.
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private Set<Usuario> usuarios = new HashSet<>();
}
