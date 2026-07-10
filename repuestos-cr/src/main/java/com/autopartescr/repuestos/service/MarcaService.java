package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Marca;
import com.autopartescr.repuestos.repository.MarcaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    @Transactional(readOnly = true)
    public List<Marca> listarMarcas() {
        return marcaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Marca buscarPorId(Integer idMarca) {
        return marcaRepository.findById(idMarca).orElse(null);
    }

    @Transactional
    public Marca guardar(Marca marca) {
        return marcaRepository.save(marca);
    }

    @Transactional
    public void eliminar(Integer idMarca) {
        marcaRepository.deleteById(idMarca);
    }
}