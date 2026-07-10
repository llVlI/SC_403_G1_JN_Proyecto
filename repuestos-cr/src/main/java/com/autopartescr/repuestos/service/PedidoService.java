package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Pedido;
import com.autopartescr.repuestos.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidos() {
        return pedidoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
public List<Pedido> listarPedidos() {
    return pedidoRepository.findAllByOrderByIdPedidoDesc();
}

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosPorCliente(Integer idCliente) {
        return pedidoRepository.findByCliente_IdCliente(idCliente);
    }

    @Transactional(readOnly = true)
    public Pedido getPedido(Integer idPedido) {
        return pedidoRepository.findById(idPedido).orElse(null);
    }

    @Transactional
    public void guardar(Pedido pedido) {
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminar(Pedido pedido) {
        pedidoRepository.delete(pedido);
    }
}