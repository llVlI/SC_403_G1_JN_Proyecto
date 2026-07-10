package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.service.PedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}