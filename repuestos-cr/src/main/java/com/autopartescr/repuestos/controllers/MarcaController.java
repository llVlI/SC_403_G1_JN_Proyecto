package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Marca;
import com.autopartescr.repuestos.service.MarcaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping("/marcas/guardar")
    public String guardarMarca(Marca marca) {
        marcaService.guardar(marca);
        return "redirect:/repuestos?marcaGuardada";
    }
}
