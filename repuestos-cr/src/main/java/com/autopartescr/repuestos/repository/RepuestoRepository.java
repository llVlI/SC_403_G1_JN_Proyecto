package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;

// TEMPORAL - borrar al integrar con el modulo de Eduardo (Repuesto real)
public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {
}
