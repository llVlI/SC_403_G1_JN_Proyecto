package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/carrito")
    public String carrito() {
        return "pedidos/carrito";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos() {
        return "pedidos/misPedidos";
    }

    @GetMapping("/gestion")
    public String gestionPedidos(Model model) {

        model.addAttribute("pedidos", pedidoService.listarPedidos());

        return "pedidos/gestionPedidos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable("id") Integer idPedido,
                                 @RequestParam("estado") String estado,
                                 RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cambiarEstado(idPedido, estado);
            redirectAttributes.addFlashAttribute("todoOk", "Estado del pedido #" + idPedido + " actualizado.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/gestion";
    }
}
