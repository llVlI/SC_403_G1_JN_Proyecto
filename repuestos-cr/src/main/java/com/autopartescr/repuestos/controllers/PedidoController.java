package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.repository.ClienteRepository;
import com.autopartescr.repuestos.repository.UsuarioRepository;
import com.autopartescr.repuestos.service.CarritoService;
import com.autopartescr.repuestos.service.PedidoService;
import com.autopartescr.repuestos.service.RepuestoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final RepuestoService repuestoService;
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public PedidoController(
            PedidoService pedidoService,
            RepuestoService repuestoService,
            CarritoService carritoService,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository) {

        this.pedidoService = pedidoService;
        this.repuestoService = repuestoService;
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model) {

        model.addAttribute(
                "carrito",
                carritoService.obtenerCarrito(session)
        );

        model.addAttribute(
                "total",
                carritoService.calcularTotal(session)
        );

        model.addAttribute(
                "cantidadArticulos",
                carritoService.calcularCantidadArticulos(session)
        );

        return "pedidos/carrito";
    }

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(
            @RequestParam("idRepuesto") Integer idRepuesto,
            @RequestParam(name = "cantidad", defaultValue = "1") Integer cantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        var repuesto = repuestoService.buscarPorId(idRepuesto);

        if (repuesto == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "El repuesto seleccionado no existe."
            );

            return "redirect:/catalogo";
        }

        try {

            carritoService.agregarProducto(
                    session,
                    repuesto,
                    cantidad
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    repuesto.getNombre() + " fue agregado al carrito."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/catalogo/" + idRepuesto;
    }

    @PostMapping("/carrito/eliminar/{id}")
    public String eliminarDelCarrito(
            @PathVariable("id") Integer idRepuesto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        carritoService.eliminarProducto(
                session,
                idRepuesto
        );

        redirectAttributes.addFlashAttribute(
                "todoOk",
                "Producto eliminado del carrito."
        );

        return "redirect:/pedidos/carrito";
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCantidad(
            @RequestParam("idRepuesto") Integer idRepuesto,
            @RequestParam("cantidad") Integer cantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {

            carritoService.actualizarCantidad(
                    session,
                    idRepuesto,
                    cantidad
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "Cantidad actualizada."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/pedidos/carrito";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos(Model model) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            return "redirect:/login";
        }

        String email = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElse(null);

        if (usuario == null) {
            return "redirect:/login";
        }

        Cliente cliente = clienteRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElse(null);

        if (cliente == null) {

            model.addAttribute(
                    "pedidos",
                    java.util.Collections.emptyList()
            );

            return "pedidos/misPedidos";
        }

        model.addAttribute(
                "pedidos",
                pedidoService.getPedidosPorCliente(
                        cliente.getIdCliente()
                )
        );

        return "pedidos/misPedidos";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelarPedido(
            @PathVariable("id") Integer idPedido,
            RedirectAttributes redirectAttributes) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Debe iniciar sesión para cancelar un pedido."
            );

            return "redirect:/login";
        }

        try {

            pedidoService.cancelarPedidoCliente(
                    idPedido,
                    authentication.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "Pedido #" + idPedido + " cancelado correctamente."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/pedidos/mis-pedidos";
    }

    @GetMapping("/gestion")
    public String gestionPedidos(Model model) {

        model.addAttribute(
                "pedidos",
                pedidoService.listarPedidos()
        );

        return "pedidos/gestionPedidos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable("id") Integer idPedido,
            @RequestParam("estado") String estado,
            RedirectAttributes redirectAttributes) {

        try {

            pedidoService.cambiarEstado(
                    idPedido,
                    estado
            );

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "Estado del pedido #" +
                    idPedido +
                    " actualizado."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/pedidos/gestion";
    }

    @PostMapping("/confirmar")
    public String confirmarPedido(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {

            pedidoService.crearPedidoDesdeCarrito(session);

            carritoService.vaciarCarrito(session);

            redirectAttributes.addFlashAttribute(
                    "todoOk",
                    "Pedido confirmado correctamente."
            );

            return "redirect:/pedidos/mis-pedidos";

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/pedidos/carrito";
        }
    }
}