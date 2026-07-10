package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.service.CategoriaService;
import com.autopartescr.repuestos.service.MarcaService;
import com.autopartescr.repuestos.service.RepuestoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RepuestoController {

    private final RepuestoService repuestoService;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;

    public RepuestoController(
            RepuestoService repuestoService,
            MarcaService marcaService,
            CategoriaService categoriaService) {
        this.repuestoService = repuestoService;
        this.marcaService = marcaService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/repuestos")
    public String listarRepuestos(Model model) {
        model.addAttribute("repuestos", repuestoService.listarRepuestos());
        model.addAttribute("marcas", marcaService.listarMarcas());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "repuestos/listado";
    }

    @GetMapping("/repuestos/nuevo")
    public String nuevoRepuesto(Model model) {
        model.addAttribute("repuesto", new Repuesto());
        model.addAttribute("marcas", marcaService.listarMarcas());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "repuestos/formulario";
    }

   @PostMapping("/repuestos/guardar")
    public String guardarRepuesto(
            Repuesto repuesto,
            @RequestParam("idMarca") Integer idMarca,
            @RequestParam("idCategoria") Integer idCategoria) {

        repuesto.setMarca(marcaService.buscarPorId(idMarca));
        repuesto.setCategoria(categoriaService.buscarPorId(idCategoria));

        repuestoService.guardar(repuesto);
        return "redirect:/repuestos?guardado";
    }

    @GetMapping("/repuestos/editar/{id}")
    public String editarRepuesto(@PathVariable("id") Integer idRepuesto, Model model) {
        Repuesto repuesto = repuestoService.buscarPorId(idRepuesto);

        if (repuesto == null) {
            return "redirect:/repuestos?error";
        }

        model.addAttribute("repuesto", repuesto);
        model.addAttribute("marcas", marcaService.listarMarcas());
        model.addAttribute("categorias", categoriaService.listarCategorias());
        return "repuestos/formulario";
    }

    @GetMapping("/repuestos/eliminar/{id}")
    public String eliminarRepuesto(@PathVariable("id") Integer idRepuesto) {
        repuestoService.eliminar(idRepuesto);
        return "redirect:/repuestos?eliminado";
    }
}