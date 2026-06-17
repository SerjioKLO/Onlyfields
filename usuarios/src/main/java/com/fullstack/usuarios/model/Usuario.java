package com.fullstack.usuarios.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa a un usuario en el sistema.
 * Esta entidad se mapea a la tabla "usuario".
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidad que representa un usuario en el sistema")
public class Usuario {

    /**
     * Identificador único del usuario (autoincremental).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del usuario", example = "1")
    private Long id;

    /**
     * Nombre completo del usuario.
     */
    @Column(nullable = false)
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez")
    private String nombre;

    /**
     * Correo electrónico del usuario, utilizado para el login. Debe ser único.
     */
    @Column(unique = true, nullable = false)
    @Schema(description = "Correo electrónico del usuario (debe ser único)", example = "juan.perez@example.com")
    private String email;

    /**
     * Hash de la contraseña del usuario. Nunca se debe almacenar la contraseña en texto plano.
     */
    @Column(name = "password_hash", nullable = false)
    @Schema(description = "Hash de la contraseña del usuario")
    private String passwordHash;

    /**
     * Estado de la cuenta del usuario (ej. "ACTIVO", "INACTIVO", "BLOQUEADO").
     * Por defecto, un nuevo usuario se crea como "ACTIVO".
     */
    @Builder.Default
    @Schema(description = "Estado de la cuenta del usuario", example = "ACTIVO")
    private String estado = "ACTIVO";

    /**
     * Fecha y hora en que se creó la cuenta del usuario.
     * Este campo no se puede actualizar una vez establecido.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Schema(description = "Fecha y hora de creación de la cuenta", example = "2024-01-01T10:00:00")
    private LocalDateTime fechaCreacion;

    /**
     * Conjunto de roles asignados al usuario.
     * Es una relación muchos a muchos con la entidad Rol, gestionada a través de una tabla intermedia "usuario_rol".
     * Se inicializa por defecto como un HashSet vacío para evitar NullPointerExceptions.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Builder.Default
    @ArraySchema(schema = @Schema(implementation = Rol.class))
    private Set<Rol> roles = new HashSet<>();

    /**
     * Callback de JPA que se ejecuta antes de que la entidad se persista por primera vez.
     * Asegura que la fecha de creación se establezca en el momento del registro.
     */
    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

    /**
     * Método de utilidad para añadir un rol al conjunto de roles del usuario de forma segura.
     * @param rol El rol a añadir.
     */
    public void agregarRol(Rol rol) {
        this.roles.add(rol);
        rol.getUsuarios().add(this);
    }

    /**
     * Método de utilidad para remover un rol del conjunto de roles del usuario.
     * @param rol El rol a remover.
     */
    public void removerRol(Rol rol) {
        this.roles.remove(rol);
        rol.getUsuarios().remove(this);
    }
}
