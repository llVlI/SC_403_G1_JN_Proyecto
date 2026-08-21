package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.ItemCarrito;
import com.autopartescr.repuestos.domain.Repuesto;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CarritoService {

    private static final String SESSION_CARRITO = "carrito";

    @SuppressWarnings("unchecked")
    public List<ItemCarrito> obtenerCarrito(HttpSession session) {

        Object carritoSesion = session.getAttribute(SESSION_CARRITO);

        if (carritoSesion == null) {
            List<ItemCarrito> carrito = new ArrayList<>();
            session.setAttribute(SESSION_CARRITO, carrito);
            return carrito;
        }

        return (List<ItemCarrito>) carritoSesion;
    }

    public void agregarProducto(HttpSession session,
                                Repuesto repuesto,
                                Integer cantidad) {

        if (repuesto == null) {
            throw new IllegalArgumentException("El repuesto no existe.");
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        if (repuesto.getStock() == null || repuesto.getStock() <= 0) {
            throw new IllegalArgumentException("El repuesto no tiene existencias disponibles.");
        }

        if (cantidad > repuesto.getStock()) {
            throw new IllegalArgumentException(
                    "La cantidad solicitada supera el stock disponible.");
        }

        List<ItemCarrito> carrito = obtenerCarrito(session);

        for (ItemCarrito item : carrito) {

            if (item.getIdRepuesto().equals(repuesto.getIdRepuesto())) {

                int nuevaCantidad = item.getCantidad() + cantidad;

                if (nuevaCantidad > repuesto.getStock()) {
                    throw new IllegalArgumentException(
                            "La cantidad total supera el stock disponible.");
                }

                item.setCantidad(nuevaCantidad);
                item.setStock(repuesto.getStock());

                session.setAttribute(SESSION_CARRITO, carrito);
                return;
            }
        }

        String nombreMarca = repuesto.getMarca() != null
                ? repuesto.getMarca().getNombre()
                : "Sin marca";

        ItemCarrito nuevoItem = new ItemCarrito(
                repuesto.getIdRepuesto(),
                repuesto.getNombre(),
                nombreMarca,
                repuesto.getPrecio(),
                repuesto.getStock(),
                cantidad
        );

        carrito.add(nuevoItem);
        session.setAttribute(SESSION_CARRITO, carrito);
    }

    public void eliminarProducto(HttpSession session,
                                 Integer idRepuesto) {

        List<ItemCarrito> carrito = obtenerCarrito(session);

        carrito.removeIf(item ->
                item.getIdRepuesto().equals(idRepuesto));

        session.setAttribute(SESSION_CARRITO, carrito);
    }

    public void actualizarCantidad(HttpSession session,
                                   Integer idRepuesto,
                                   Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero.");
        }

        List<ItemCarrito> carrito = obtenerCarrito(session);

        for (ItemCarrito item : carrito) {

            if (item.getIdRepuesto().equals(idRepuesto)) {

                if (cantidad > item.getStock()) {
                    throw new IllegalArgumentException(
                            "La cantidad supera el stock disponible.");
                }

                item.setCantidad(cantidad);
                break;
            }
        }

        session.setAttribute(SESSION_CARRITO, carrito);
    }

    public BigDecimal calcularTotal(HttpSession session) {

        return obtenerCarrito(session)
                .stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int calcularCantidadArticulos(HttpSession session) {

        return obtenerCarrito(session)
                .stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
    }

    public boolean estaVacio(HttpSession session) {
        return obtenerCarrito(session).isEmpty();
    }

    public void vaciarCarrito(HttpSession session) {
        session.removeAttribute(SESSION_CARRITO);
    }
}