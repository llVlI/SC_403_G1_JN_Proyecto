package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByUsuario_IdUsuario(Integer idUsuario);
}
