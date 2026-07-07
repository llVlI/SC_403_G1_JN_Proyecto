package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Inventario;
import com.autopartescr.repuestos.repository.InventarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public InventarioService(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
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
    public void actualizarStock(Integer id, Integer nuevaCantidad) {
        var inventarioOpt = inventarioRepository.findById(id);
        if (inventarioOpt.isPresent()) {
            var inventario = inventarioOpt.get();
            inventario.setCantidadActual(nuevaCantidad);
            inventarioRepository.save(inventario);
        }
    }
}
