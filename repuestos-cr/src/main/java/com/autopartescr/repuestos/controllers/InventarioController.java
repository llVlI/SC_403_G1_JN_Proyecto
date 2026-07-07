package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Inventario;
import com.autopartescr.repuestos.service.InventarioService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final MessageSource messageSource;

    public InventarioController(InventarioService inventarioService, MessageSource messageSource) {
        this.inventarioService = inventarioService;
        this.messageSource = messageSource;
    }

    // Reporte de inventario con alerta de bajo stock (HU-08)
    @GetMapping
    public String listarInventario(Model model) {
        var lista = inventarioService.getInventario();
        model.addAttribute("inventario", lista);

        int alertas = 0;
        for (Inventario item : lista) {
            if (item.getCantidadActual() < item.getCantidadMinima()) {
                alertas++;
            }
        }
        model.addAttribute("totalAlertas", alertas);
        return "inventario/listado";
    }

    // Formulario para actualizar stock (HU-07)
    @GetMapping("/actualizar/{id}")
    public String mostrarFormularioActualizar(@PathVariable Integer id, Model model) {
        var invOpt = inventarioService.getInventario(id);
        if (invOpt.isEmpty()) {
            return "redirect:/inventario";
        }
        model.addAttribute("inventarioItem", invOpt.get());
        return "inventario/formulario";
    }

    @PostMapping("/guardar")
    public String guardarStock(@ModelAttribute Inventario inventarioItem,
                                RedirectAttributes redirectAttributes) {
        inventarioService.actualizarStock(inventarioItem.getIdInventario(), inventarioItem.getCantidadActual());
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("inventario.mensaje.actualizado", null, Locale.getDefault()));
        return "redirect:/inventario";
    }
}
