package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Marca;
import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.repository.InventarioRepository;
import com.autopartescr.repuestos.repository.MarcaRepository;
import com.autopartescr.repuestos.repository.RepuestoRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catalogo del Cliente (Santiago) - HU-11, HU-12, HU-13, HU-14.
 *
 * Este servicio SOLO LEE informacion de las entidades de catalogo
 * (Repuesto, Marca) que pertenecen al modulo de Eduardo, y del
 * Inventario que pertenece al modulo de Adrian. Santiago no es dueno de
 * ninguna tabla nueva.
 */
@Service
public class CatalogoService {

    private final RepuestoRepository repuestoRepository;
    private final MarcaRepository marcaRepository;
    private final InventarioRepository inventarioRepository;

    public CatalogoService(RepuestoRepository repuestoRepository,
                            MarcaRepository marcaRepository,
                            InventarioRepository inventarioRepository) {
        this.repuestoRepository = repuestoRepository;
        this.marcaRepository = marcaRepository;
        this.inventarioRepository = inventarioRepository;
    }

    // Para el combo/filtro de marcas de la Pantalla 3
    @Transactional(readOnly = true)
    public List<Marca> listarMarcas() {
        return marcaRepository.findAll();
    }

    // Pantalla 2 (Inicio): unos cuantos repuestos destacados
    @Transactional(readOnly = true)
    public List<RepuestoCatalogoDTO> listarDestacados(int cantidad) {
        var todos = armarCatalogo(repuestoRepository.findAll());
        if (todos.size() <= cantidad) {
            return todos;
        }
        return todos.subList(0, cantidad);
    }

    /**
     * Pantalla 3 (Catalogo).
     * HU-11: buscar por texto (nombre del repuesto).
     * HU-12: filtrar por marca.
     * HU-13: mostrar/filtrar por disponibilidad (stock > 0).
     *
     * Los tres criterios son opcionales y se pueden combinar entre si.
     */
    @Transactional(readOnly = true)
    public List<RepuestoCatalogoDTO> buscarCatalogo(String texto, Integer idMarca, Boolean soloDisponibles) {
        String textoBusqueda = (texto == null) ? null : texto.trim();
        boolean hayTexto = textoBusqueda != null && !textoBusqueda.isEmpty();
        boolean hayMarca = idMarca != null;

        List<Repuesto> base;
        if (hayTexto && hayMarca) {
            base = repuestoRepository.findByNombreContainingIgnoreCaseAndMarca_IdMarca(textoBusqueda, idMarca);
        } else if (hayTexto) {
            base = repuestoRepository.findByNombreContainingIgnoreCase(textoBusqueda);
        } else if (hayMarca) {
            base = repuestoRepository.findByMarca_IdMarca(idMarca);
        } else {
            base = repuestoRepository.findAll();
        }

        List<RepuestoCatalogoDTO> catalogo = armarCatalogo(base);

        if (soloDisponibles != null && soloDisponibles) {
            catalogo.removeIf(item -> !item.isDisponible());
        }
        return catalogo;
    }

    // Pantalla 4 (Detalle de repuesto) - HU-14
    @Transactional(readOnly = true)
    public Optional<RepuestoCatalogoDTO> obtenerDetalle(Integer idRepuesto) {
        return repuestoRepository.findById(idRepuesto)
                .map(this::armarItemCatalogo);
    }

    // Combina cada Repuesto con su stock actual en Inventario (HU-13)
    private List<RepuestoCatalogoDTO> armarCatalogo(List<Repuesto> repuestos) {
        List<RepuestoCatalogoDTO> catalogo = new ArrayList<>();
        for (Repuesto repuesto : repuestos) {
            catalogo.add(armarItemCatalogo(repuesto));
        }
        return catalogo;
    }

    private RepuestoCatalogoDTO armarItemCatalogo(Repuesto repuesto) {
        Integer stock = inventarioRepository.findByRepuesto_IdRepuesto(repuesto.getIdRepuesto())
                .map(inv -> inv.getCantidadActual())
                .orElse(0);
        boolean disponible = stock != null && stock > 0;
        return new RepuestoCatalogoDTO(repuesto, stock, disponible);
    }
}
