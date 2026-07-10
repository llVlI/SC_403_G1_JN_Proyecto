package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Repuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    List<Repuesto> findAllByOrderByIdRepuestoDesc();

    boolean existsByCodigo(String codigo);
}