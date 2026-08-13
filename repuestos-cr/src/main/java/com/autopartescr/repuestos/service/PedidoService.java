package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Pedido;
import com.autopartescr.repuestos.repository.EstadoPedidoRepository;
import com.autopartescr.repuestos.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, EstadoPedidoRepository estadoPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.estadoPedidoRepository = estadoPedidoRepository;
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

 
    @Transactional
    public void cambiarEstado(Integer idPedido, String nombreEstado) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido));

        var estado = estadoPedidoRepository.findByNombre(nombreEstado)
                .orElseThrow(() -> new IllegalArgumentException("Estado no válido: " + nombreEstado));

        pedido.setEstadoPedido(estado);
        pedidoRepository.save(pedido);
    }
}
