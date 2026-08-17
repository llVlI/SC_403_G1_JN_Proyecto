package com.autopartescr.repuestos.repository;

import com.autopartescr.repuestos.domain.Ruta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RutaRepository extends JpaRepository<Ruta, Integer> {

    // Se ordenan primero las publicas (requiereRol = false) para que
    // SecurityConfig las registre antes que las protegidas, siguiendo
    // el orden que exige Spring Security al armar las reglas.
    List<Ruta> findAllByOrderByRequiereRolAsc();
}
