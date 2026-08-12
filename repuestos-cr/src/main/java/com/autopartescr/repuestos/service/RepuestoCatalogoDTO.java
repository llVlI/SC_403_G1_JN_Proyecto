package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Repuesto;

/**
 * Objeto de solo lectura para la Pantalla 3 (Catalogo) y la Pantalla 4
 * (Detalle de repuesto).
 *
 * Santiago no crea ninguna tabla nueva: este DTO simplemente combina, en
 * memoria, la informacion del Repuesto (modulo de Eduardo) con el stock
 * actual del Inventario (modulo de Adrian) para poder mostrar la
 * disponibilidad (HU-13) sin duplicar datos en la base de datos.
 */
public class RepuestoCatalogoDTO {

    private final Repuesto repuesto;
    private final Integer stockActual;
    private final boolean disponible;

    public RepuestoCatalogoDTO(Repuesto repuesto, Integer stockActual, boolean disponible) {
        this.repuesto = repuesto;
        this.stockActual = stockActual;
        this.disponible = disponible;
    }

    public Repuesto getRepuesto() {
        return repuesto;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
