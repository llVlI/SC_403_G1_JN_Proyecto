package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "inventario")
public class Inventario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idInventario;

    @ManyToOne
    @JoinColumn(name = "repuesto_id", nullable = false, unique = true)
    @NotNull
    private Repuesto repuesto;

    @Column(name = "cantidad_actual", nullable = false)
    @NotNull
    @Min(0)
    private Integer cantidadActual;

    // alerta de bajo stock (HU-08).
    // La comparacion (cantidadActual < cantidadMinima) se hace directamente
    // en la vista Thymeleaf.
    @Column(name = "cantidad_minima", nullable = false)
    @NotNull
    @Min(0)
    private Integer cantidadMinima;
}
