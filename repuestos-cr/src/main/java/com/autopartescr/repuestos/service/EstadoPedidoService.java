package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.EstadoPedido;
import com.autopartescr.repuestos.repository.EstadoPedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadoPedidoService {

    private final EstadoPedidoRepository estadoPedidoRepository;

    public EstadoPedidoService(EstadoPedidoRepository estadoPedidoRepository) {
        this.estadoPedidoRepository = estadoPedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<EstadoPedido> getEstadosPedido() {
        return estadoPedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EstadoPedido getEstadoPedido(Integer idEstadoPedido) {
        return estadoPedidoRepository.findById(idEstadoPedido).orElse(null);
    }
}