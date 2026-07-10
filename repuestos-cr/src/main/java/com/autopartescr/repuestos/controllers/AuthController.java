package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    // Nombre del atributo de sesion donde guardamos al usuario logueado.
    public static final String SESSION_USUARIO = "usuarioLogueado";

    public AuthController(UsuarioService usuarioService, MessageSource messageSource) {
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    // ---------- LOGIN (HU-10) ----------

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute Usuario usuario,
                                 HttpSession session,
                                 Model model) {
        var usuarioOpt = usuarioService.login(usuario.getEmail(), usuario.getPassword());
        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", messageSource.getMessage("login.error", null, Locale.getDefault()));
            return "auth/login";
        }
        Usuario usuarioLogueado = usuarioOpt.get();
        session.setAttribute(SESSION_USUARIO, usuarioLogueado);

        // Redirige segun el rol: solo el administrador va directo a
        // inventario, el resto va al inicio.
        if ("ADMINISTRADOR".equals(usuarioLogueado.getRol().getNombre())) {
            return "redirect:/inventario";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
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
