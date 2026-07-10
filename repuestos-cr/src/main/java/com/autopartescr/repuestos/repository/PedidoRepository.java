package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findAllByOrderByIdPedidoDesc();

    List<Pedido> findByCliente_IdCliente(Integer idCliente);

}