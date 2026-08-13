package com.autopartescr.repuestos.controllers;

import com.autopartescr.repuestos.domain.Categoria;
import com.autopartescr.repuestos.domain.Marca;
import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.service.CategoriaService;
import com.autopartescr.repuestos.service.MarcaService;
import com.autopartescr.repuestos.service.RepuestoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

        model.addAttribute(
                "repuestos",
                repuestoService.listarRepuestos()
        );

        model.addAttribute(
                "marcas",
                marcaService.listarMarcas()
        );

        model.addAttribute(
                "categorias",
                categoriaService.listarCategorias()
        );

        return "repuestos/listado";
    }

    @GetMapping("/repuestos/nuevo")
    public String nuevoRepuesto(Model model) {

        model.addAttribute(
                "repuesto",
                new Repuesto()
        );

        cargarListas(model);

        return "repuestos/formulario";
    }

    @PostMapping("/repuestos/guardar")
    public String guardarRepuesto(
            @Valid Repuesto repuesto,
            BindingResult resultado,
            @RequestParam(value = "idMarca", required = false)
            Integer idMarca,
            @RequestParam(value = "idCategoria", required = false)
            Integer idCategoria,
            Model model) {

        /*
         * Validar código repetido
         */
        if (repuesto.getCodigo() != null
                && !repuesto.getCodigo().isBlank()) {

            repuesto.setCodigo(
                    repuesto.getCodigo().trim()
            );

            boolean codigoRepetido;

            if (repuesto.getIdRepuesto() == null) {

                codigoRepetido
                        = repuestoService.existeCodigo(
                                repuesto.getCodigo()
                        );

            } else {

                codigoRepetido
                        = repuestoService.existeCodigoEnOtroRepuesto(
                                repuesto.getCodigo(),
                                repuesto.getIdRepuesto()
                        );
            }

            if (codigoRepetido) {

                resultado.rejectValue(
                        "codigo",
                        "codigo.duplicado",
                        "Ya existe un repuesto con este código"
                );
            }
        }

        /*
         * Validar marca
         */
        Marca marca = null;

        if (idMarca == null) {

            resultado.reject(
                    "marca.obligatoria",
                    "Debe seleccionar una marca"
            );

        } else {

            marca = marcaService.buscarPorId(idMarca);

            if (marca == null) {

                resultado.reject(
                        "marca.invalida",
                        "La marca seleccionada no existe"
                );
            }
        }

        /*
         * Validar categoría
         */
        Categoria categoria = null;

        if (idCategoria == null) {

            resultado.reject(
                    "categoria.obligatoria",
                    "Debe seleccionar una categoría"
            );

        } else {

            categoria
                    = categoriaService.buscarPorId(
                            idCategoria
                    );

            if (categoria == null) {

                resultado.reject(
                        "categoria.invalida",
                        "La categoría seleccionada no existe"
                );
            }
        }

        /*
         * Si hay errores vuelve al formulario
         */
        if (resultado.hasErrors()) {

            if (marca != null) {
                repuesto.setMarca(marca);
            }

            if (categoria != null) {
                repuesto.setCategoria(categoria);
            }

            cargarListas(model);

            return "repuestos/formulario";
        }

        /*
         * Asociar marca y categoría
         */
        repuesto.setMarca(marca);
        repuesto.setCategoria(categoria);

        /*
         * Guardar
         */
        repuestoService.guardar(repuesto);

        return "redirect:/repuestos?guardado";
    }

    @GetMapping("/repuestos/editar/{id}")
    public String editarRepuesto(
            @PathVariable("id") Integer idRepuesto,
            Model model) {

        Repuesto repuesto
                = repuestoService.buscarPorId(
                        idRepuesto
                );

        if (repuesto == null) {
            return "redirect:/repuestos?error";
        }

        model.addAttribute(
                "repuesto",
                repuesto
        );

        cargarListas(model);

        return "repuestos/formulario";
    }

    @GetMapping("/repuestos/eliminar/{id}")
    public String eliminarRepuesto(
            @PathVariable("id") Integer idRepuesto) {

        Repuesto repuesto
                = repuestoService.buscarPorId(
                        idRepuesto
                );

        if (repuesto == null) {
            return "redirect:/repuestos?error";
        }

        repuestoService.eliminar(idRepuesto);

        return "redirect:/repuestos?eliminado";
    }

    private void cargarListas(Model model) {

        model.addAttribute(
                "marcas",
                marcaService.listarMarcas()
        );

        model.addAttribute(
                "categorias",
                categoriaService.listarCategorias()
        );
    }
}