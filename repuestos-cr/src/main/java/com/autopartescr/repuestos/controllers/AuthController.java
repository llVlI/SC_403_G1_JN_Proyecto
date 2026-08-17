package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.service.UsuarioService;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// El login y el logout ya NO se manejan aqui: los gestiona Spring
// Security directamente (ver SecurityConfig, ruta /login configurada
// con formLogin). Este controller solo se encarga del registro publico
// de clientes nuevos (HU-09).
@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public AuthController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    // La pantalla de login la sirve Spring Security (loginPage("/login")
    // en SecurityConfig), pero seguimos necesitando este metodo para que
    // Thymeleaf pueda mostrar la vista auth/login.html cuando el usuario
    // visita /login o cuando falla el login (?error=true).
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/login";
    }

    // Pagina que se muestra cuando un usuario logueado intenta entrar a
    // una ruta protegida para la que no tiene el rol necesario
    // (ver accessDeniedPage en SecurityConfig).
    @GetMapping("/acceso-denegado")
    public String mostrarAccesoDenegado() {
        return "auth/acceso-denegado";
    }

    // ---------- REGISTRO (HU-09) ----------

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        Cliente cliente = new Cliente();
        cliente.setUsuario(new Usuario());
        model.addAttribute("cliente", cliente);
        return "auth/registro";
    }

    @PostMapping("/registro/guardar")
    public String guardarRegistro(@ModelAttribute Cliente cliente, Model model,
                                   RedirectAttributes redirectAttributes) {
        if (usuarioService.existeEmail(cliente.getUsuario().getEmail())) {
            model.addAttribute("error", messageSource.getMessage("registro.error.correo", null, Locale.getDefault()));
            model.addAttribute("cliente", cliente);
            return "auth/registro";
        }
        usuarioService.registrarCliente(cliente);
        redirectAttributes.addFlashAttribute("todoOk",
                messageSource.getMessage("registro.mensaje.ok", null, Locale.getDefault()));
        return "redirect:/login";
    }
}
