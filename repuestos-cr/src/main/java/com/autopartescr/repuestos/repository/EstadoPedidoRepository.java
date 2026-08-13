package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.EstadoPedido;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Integer> {

    Optional<EstadoPedido> findByNombre(String nombre);
}
