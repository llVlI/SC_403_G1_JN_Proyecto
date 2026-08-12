package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.service.CatalogoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Catalogo del Cliente (Santiago).
 * Pantalla 2 = Inicio, Pantalla 3 = Catalogo, Pantalla 4 = Detalle Repuesto.
 */
@Controller
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // Pantalla 2 - Inicio
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("destacados", catalogoService.listarDestacados(4));
        model.addAttribute("marcas", catalogoService.listarMarcas());
        return "index";
    }

    // Pantalla 3 - Catalogo (HU-11 buscar, HU-12 filtrar por marca, HU-13 disponibilidad)
    @GetMapping("/catalogo")
    public String verCatalogo(@RequestParam(name = "q", required = false) String q,
                               @RequestParam(name = "marca", required = false) Integer marca,
                               @RequestParam(name = "disponibles", required = false) Boolean disponibles,
                               Model model) {
        model.addAttribute("repuestos", catalogoService.buscarCatalogo(q, marca, disponibles));
        model.addAttribute("marcas", catalogoService.listarMarcas());
        model.addAttribute("q", q);
        model.addAttribute("marcaSeleccionada", marca);
        model.addAttribute("soloDisponibles", disponibles != null && disponibles);
        return "catalogo/listado";
    }

    // Pantalla 4 - Detalle Repuesto (HU-14)
    @GetMapping("/catalogo/{id}")
    public String verDetalle(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        var detalle = catalogoService.obtenerDetalle(id);
        if (detalle.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El repuesto solicitado no existe.");
            return "redirect:/catalogo";
        }
        model.addAttribute("item", detalle.get());
        return "catalogo/detalle";
    }
}
