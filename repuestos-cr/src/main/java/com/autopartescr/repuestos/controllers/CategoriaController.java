package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Categoria;
import com.autopartescr.repuestos.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(
            Categoria categoria,
            RedirectAttributes redirectAttributes) {

        /*
         * Validar nombre vacío
         */
        if (categoria.getNombre() == null
                || categoria.getNombre().isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorCategoria",
                    "Debe ingresar el nombre de la categoría"
            );

            return "redirect:/repuestos";
        }

        /*
         * Quitar espacios al inicio y al final
         */
        categoria.setNombre(
                categoria.getNombre().trim()
        );

        /*
         * Validar longitud
         */
        if (categoria.getNombre().length() > 80) {

            redirectAttributes.addFlashAttribute(
                    "errorCategoria",
                    "El nombre de la categoría no puede superar los 80 caracteres"
            );

            return "redirect:/repuestos";
        }

        /*
         * Validar categoría repetida
         */
        boolean repetida;

        if (categoria.getIdCategoria() == null) {

            repetida = categoriaService.existeNombre(
                    categoria.getNombre()
            );

        } else {

            repetida = categoriaService.existeNombreEnOtraCategoria(
                    categoria.getNombre(),
                    categoria.getIdCategoria()
            );
        }

        if (repetida) {

            redirectAttributes.addFlashAttribute(
                    "errorCategoria",
                    "Ya existe una categoría con ese nombre"
            );

            return "redirect:/repuestos";
        }

        /*
         * Guardar categoría
         */
        categoriaService.guardar(categoria);

        redirectAttributes.addFlashAttribute(
                "categoriaGuardada",
                true
        );

        return "redirect:/repuestos";
    }
}