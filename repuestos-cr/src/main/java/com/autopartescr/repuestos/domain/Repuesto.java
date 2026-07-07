package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

/**
 * TEMPORAL.
 * Esta clase existe solo para que el modulo de Inventario compile y se
 * pueda probar de forma independiente mientras Eduardo desarrolla el
 * modulo de Catalogo (Repuesto, Marca, Categoria).
 *
 * Cuando se integren los modulos:
 * 1. Borrar este archivo.
 * 2. Usar la clase Repuesto real del paquete de Eduardo.
 * 3. Mientras la tabla se llame "repuesto" y tenga las mismas columnas
 *    (id, nombre), Hibernate no tendra problema en mapear la relacion
 *    de Inventario hacia el Repuesto real.
 */
@Data
@Entity
@Table(name = "repuesto")
public class Repuesto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idRepuesto;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombre;
}
