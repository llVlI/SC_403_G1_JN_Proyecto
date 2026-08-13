package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Marca;
import com.autopartescr.repuestos.service.MarcaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MarcaController {

    private final MarcaService marcaService;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping("/marcas/guardar")
    public String guardarMarca(
            Marca marca,
            RedirectAttributes redirectAttributes) {

        /*
         * Validar nombre vacío
         */
        if (marca.getNombre() == null
                || marca.getNombre().isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorMarca",
                    "Debe ingresar el nombre de la marca"
            );

            return "redirect:/repuestos";
        }

        /*
         * Quitar espacios innecesarios
         */
        marca.setNombre(
                marca.getNombre().trim()
        );

        /*
         * Validar marca repetida
         */
        boolean repetida;

        if (marca.getIdMarca() == null) {

            repetida = marcaService.existeNombre(
                    marca.getNombre()
            );

        } else {

            repetida = marcaService.existeNombreEnOtraMarca(
                    marca.getNombre(),
                    marca.getIdMarca()
            );
        }

        if (repetida) {

            redirectAttributes.addFlashAttribute(
                    "errorMarca",
                    "Ya existe una marca con ese nombre"
            );

            return "redirect:/repuestos";
        }

        /*
         * Guardar marca
         */
        marcaService.guardar(marca);

        redirectAttributes.addFlashAttribute(
                "marcaGuardada",
                true
        );

        return "redirect:/repuestos";
    }
}