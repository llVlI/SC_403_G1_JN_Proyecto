package com.autopartescr.repuestos.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

// Representa una ruta protegida de la aplicacion (HU de seguridad).
// En vez de escribir las reglas de acceso directamente en el codigo de
// SecurityConfig, se guardan en la base de datos. Al arrancar la
// aplicacion, SecurityConfig lee esta tabla y arma las reglas de acceso.
@Data
@Entity
@Table(name = "ruta")
public class Ruta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer idRuta;

    // Patron de la URL protegida, por ejemplo "/inventario/**"
    @Column(name = "ruta", nullable = false, length = 100)
    private String ruta;

    // Si es false, la ruta es publica (no requiere estar logueado).
    // Si es true, requiere el rol indicado en "rol".
    @Column(name = "requiere_rol", nullable = false)
    private boolean requiereRol;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;
}
