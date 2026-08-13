package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Inventario;
import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.repository.InventarioRepository;
import com.autopartescr.repuestos.repository.RepuestoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final RepuestoRepository repuestoRepository;

    public InventarioService(
            InventarioRepository inventarioRepository,
            RepuestoRepository repuestoRepository) {

        this.inventarioRepository = inventarioRepository;
        this.repuestoRepository = repuestoRepository;
    }

    @Transactional(readOnly = true)
    public List<Inventario> getInventario() {
        return inventarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Inventario> getInventario(Integer id) {
        return inventarioRepository.findById(id);
    }

    @Transactional
    public void actualizarStock(
            Integer id,
            Integer nuevaCantidad,
            Integer nuevaCantidadMinima) {

        if (nuevaCantidad == null
                || nuevaCantidadMinima == null
                || nuevaCantidad < 0
                || nuevaCantidadMinima < 0) {

            return;
        }

        Optional<Inventario> inventarioOpt
                = inventarioRepository.findById(id);

        if (inventarioOpt.isPresent()) {

            Inventario inventario = inventarioOpt.get();

            /*
             * Actualizar stock actual
             */
            inventario.setCantidadActual(nuevaCantidad);

            /*
             * Actualizar stock mínimo
             */
            inventario.setCantidadMinima(nuevaCantidadMinima);

            inventarioRepository.save(inventario);

            /*
             * Mantener sincronizado Repuesto.stock
             */
            Repuesto repuesto = inventario.getRepuesto();

            if (repuesto != null) {

                repuesto.setStock(nuevaCantidad);

                repuestoRepository.save(repuesto);
            }
        }
    }
}