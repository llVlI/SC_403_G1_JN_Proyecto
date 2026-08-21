package com.autopartescr.repuestos.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ItemCarrito implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer idRepuesto;
    private String nombre;
    private String marca;
    private BigDecimal precio;
    private Integer stock;
    private Integer cantidad;

    public ItemCarrito() {
    }

    public ItemCarrito(
            Integer idRepuesto,
            String nombre,
            String marca,
            BigDecimal precio,
            Integer stock,
            Integer cantidad) {

        this.idRepuesto = idRepuesto;
        this.nombre = nombre;
        this.marca = marca;
        this.precio = precio;
        this.stock = stock;
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {

        if (precio == null || cantidad == null) {
            return BigDecimal.ZERO;
        }

        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}