package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "cliente")
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idCliente;

    @Column(name = "telefono", length = 20)
    @Size(max = 20)
    private String telefono;

    @Column(name = "direccion", length = 200)
    @Size(max = 200)
    private String direccion;

    // Un Cliente extiende a un Usuario con rol CLIENTE.
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @NotNull
    private Usuario usuario;
}
