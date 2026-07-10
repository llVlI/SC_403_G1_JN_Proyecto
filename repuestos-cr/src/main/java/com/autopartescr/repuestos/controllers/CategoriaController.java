package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Categoria;
import com.autopartescr.repuestos.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(Categoria categoria) {
        categoriaService.guardar(categoria);
        return "redirect:/repuestos?categoriaGuardada";
    }
}