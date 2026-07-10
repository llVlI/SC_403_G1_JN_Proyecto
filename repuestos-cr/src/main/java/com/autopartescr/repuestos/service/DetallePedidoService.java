package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.DetallePedido;
import com.autopartescr.repuestos.repository.DetallePedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    public DetallePedidoService(DetallePedidoRepository detallePedidoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> getDetallesPorPedido(Integer idPedido) {
        return detallePedidoRepository.findByPedido_IdPedido(idPedido);
    }

    @Transactional(readOnly = true)
    public DetallePedido getDetallePedido(Integer idDetallePedido) {
        return detallePedidoRepository.findById(idDetallePedido).orElse(null);
    }

    @Transactional
    public void guardar(DetallePedido detallePedido) {
        detallePedidoRepository.save(detallePedido);
    }

    @Transactional
    public void eliminar(DetallePedido detallePedido) {
        detallePedidoRepository.delete(detallePedido);
    }
}