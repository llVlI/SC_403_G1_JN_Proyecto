package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "rol")
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idRol;

    // Valores esperados: ADMINISTRADOR, CLIENTE, ENCARGADO_VENTAS
    @Column(name = "nombre", nullable = false, length = 40, unique = true)
    @NotNull
    @Size(max = 40)
    private String nombre;

    @OneToMany(mappedBy = "rol")
    private List<Usuario> usuarios;
}
