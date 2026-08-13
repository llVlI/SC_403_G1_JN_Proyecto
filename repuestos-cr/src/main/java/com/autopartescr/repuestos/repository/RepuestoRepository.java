package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Repuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {

    List<Repuesto> findAllByOrderByIdRepuestoDesc();

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdRepuestoNot(String codigo, Integer idRepuesto);

    // Usados por el catálogo del cliente
    List<Repuesto> findByNombreContainingIgnoreCase(String nombre);

    List<Repuesto> findByMarca_IdMarca(Integer idMarca);

    List<Repuesto> findByNombreContainingIgnoreCaseAndMarca_IdMarca(
            String nombre,
            Integer idMarca
    );
}