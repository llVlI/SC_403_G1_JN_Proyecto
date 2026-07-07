package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 120, unique = true)
    @NotNull
    @Size(max = 120)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String password;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
}
