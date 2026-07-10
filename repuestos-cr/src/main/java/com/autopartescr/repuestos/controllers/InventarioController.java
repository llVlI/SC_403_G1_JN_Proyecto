package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Inventario;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.service.InventarioService;
import jakarta.servlet.http.HttpSession;
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

    private String redireccionSiNoAutorizado(HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute(AuthController.SESSION_USUARIO);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!"ADMINISTRADOR".equals(usuario.getRol().getNombre())) {
            if (redirectAttributes != null) {
                redirectAttributes.addFlashAttribute("error",
                        "No tienes permiso para acceder al inventario.");
            }
            return "redirect:/";
        }
        return null;
    }

    @GetMapping
    public String listarInventario(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String redireccion = redireccionSiNoAutorizado(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

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

    @GetMapping("/actualizar/{id}")
    public String mostrarFormularioActualizar(@PathVariable Integer id, Model model,
                                               HttpSession session, RedirectAttributes redirectAttributes) {
        String redireccion = redireccionSiNoAutorizado(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        var invOpt = inventarioService.getInventario(id);
        if (invOpt.isEmpty()) {
            return "redirect:/inventario";
        }
        model.addAttribute("inventarioItem", invOpt.get());
        return "inventario/formulario";
    }

    @PostMapping("/guardar")
    public String guardarStock(@ModelAttribute Inventario inventarioItem,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        String redireccion = redireccionSiNoAutorizado(session, redirectAttributes);
        if (redireccion != null) {
            return redireccion;
        }

        inventarioService.actualizarStock(inventarioItem.getIdInventario(), inventarioItem.getCantidadActual());
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("inventario.mensaje.actualizado", null, Locale.getDefault()));
        return "redirect:/inventario";
    }
}