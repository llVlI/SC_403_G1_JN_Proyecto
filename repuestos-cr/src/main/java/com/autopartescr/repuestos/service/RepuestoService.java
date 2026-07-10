package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.repository.RepuestoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;

    public RepuestoService(RepuestoRepository repuestoRepository) {
        this.repuestoRepository = repuestoRepository;
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
        return repuestoRepository.save(repuesto);
    }

    @Transactional
    public void eliminar(Integer idRepuesto) {
        repuestoRepository.deleteById(idRepuesto);
    }

    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return repuestoRepository.existsByCodigo(codigo);
    }
}
