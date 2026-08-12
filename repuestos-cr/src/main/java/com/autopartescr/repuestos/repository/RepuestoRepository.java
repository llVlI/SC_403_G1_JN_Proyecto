package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Repuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    List<Repuesto> findAllByOrderByIdRepuestoDesc();

    boolean existsByCodigo(String codigo);

    // Usados por el Catalogo del Cliente (Santiago) - HU-11 y HU-12
    List<Repuesto> findByNombreContainingIgnoreCase(String nombre);

    List<Repuesto> findByMarca_IdMarca(Integer idMarca);

    List<Repuesto> findByNombreContainingIgnoreCaseAndMarca_IdMarca(String nombre, Integer idMarca);
}