package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Marca;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    List<Marca> findAllByOrderByNombreAsc();
}