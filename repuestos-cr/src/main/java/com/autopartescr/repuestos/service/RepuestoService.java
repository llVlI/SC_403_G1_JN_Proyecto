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
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;
    private final InventarioRepository inventarioRepository;

    public RepuestoService(
            RepuestoRepository repuestoRepository,
            InventarioRepository inventarioRepository) {

        this.repuestoRepository = repuestoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Repuesto> listarRepuestos() {
        return repuestoRepository.findAllByOrderByIdRepuestoDesc();
    }

    @Transactional(readOnly = true)
    public Repuesto buscarPorId(Integer idRepuesto) {
        return repuestoRepository.findById(idRepuesto).orElse(null);
    }

    @Transactional
    public Repuesto guardar(Repuesto repuesto) {

        /*
         * Guardar primero el repuesto
         */
        Repuesto repuestoGuardado
                = repuestoRepository.save(repuesto);

        /*
         * Buscar si ya tiene inventario
         */
        Optional<Inventario> inventarioOpt
                = inventarioRepository
                        .findByRepuesto_IdRepuesto(
                                repuestoGuardado.getIdRepuesto()
                        );

        Inventario inventario;

        if (inventarioOpt.isPresent()) {

            /*
             * Si ya existe, actualizar stock
             */
            inventario = inventarioOpt.get();

            inventario.setCantidadActual(
                    repuestoGuardado.getStock()
            );

        } else {

            /*
             * Si es un repuesto nuevo,
             * crear automáticamente su inventario
             */
            inventario = new Inventario();

            inventario.setRepuesto(repuestoGuardado);

            inventario.setCantidadActual(
                    repuestoGuardado.getStock()
            );

            /*
             * Por ahora inicia en 0.
             * En HU-08 permitiremos configurar
             * el stock mínimo.
             */
            inventario.setCantidadMinima(0);
        }

        inventarioRepository.save(inventario);

        return repuestoGuardado;
    }

    @Transactional
    public void eliminar(Integer idRepuesto) {

        /*
         * Eliminar primero su inventario
         * para evitar problemas con la FK.
         */
        Optional<Inventario> inventarioOpt
                = inventarioRepository
                        .findByRepuesto_IdRepuesto(idRepuesto);

        if (inventarioOpt.isPresent()) {
            inventarioRepository.delete(
                    inventarioOpt.get()
            );
        }

        repuestoRepository.deleteById(idRepuesto);
    }

    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return repuestoRepository.existsByCodigo(codigo);
    }

    @Transactional(readOnly = true)
    public boolean existeCodigoEnOtroRepuesto(
            String codigo,
            Integer idRepuesto) {

        return repuestoRepository
                .existsByCodigoAndIdRepuestoNot(
                        codigo,
                        idRepuesto
                );
    }
}