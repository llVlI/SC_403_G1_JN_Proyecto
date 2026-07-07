package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByRepuesto_IdRepuesto(Integer idRepuesto);
}
