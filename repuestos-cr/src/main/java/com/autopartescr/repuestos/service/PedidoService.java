package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.DetallePedido;
import com.autopartescr.repuestos.domain.Inventario;
import com.autopartescr.repuestos.domain.ItemCarrito;
import com.autopartescr.repuestos.domain.Pedido;
import com.autopartescr.repuestos.domain.Repuesto;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.repository.ClienteRepository;
import com.autopartescr.repuestos.repository.DetallePedidoRepository;
import com.autopartescr.repuestos.repository.EstadoPedidoRepository;
import com.autopartescr.repuestos.repository.InventarioRepository;
import com.autopartescr.repuestos.repository.PedidoRepository;
import com.autopartescr.repuestos.repository.RepuestoRepository;
import com.autopartescr.repuestos.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final RepuestoRepository repuestoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final CarritoService carritoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            EstadoPedidoRepository estadoPedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            RepuestoRepository repuestoRepository,
            InventarioRepository inventarioRepository,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            CarritoService carritoService) {

        this.pedidoRepository = pedidoRepository;
        this.estadoPedidoRepository = estadoPedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.repuestoRepository = repuestoRepository;
        this.inventarioRepository = inventarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.carritoService = carritoService;
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAllByOrderByIdPedidoDesc();
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosPorCliente(Integer idCliente) {
        return pedidoRepository.findByCliente_IdCliente(idCliente);
    }

    @Transactional(readOnly = true)
    public Pedido getPedido(Integer idPedido) {
        return pedidoRepository.findById(idPedido).orElse(null);
    }

    @Transactional
    public void guardar(Pedido pedido) {
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminar(Pedido pedido) {
        pedidoRepository.delete(pedido);
    }

    /*
     * =========================================================
     * CREAR PEDIDO DESDE EL CARRITO
     * =========================================================
     */
    @Transactional
    public Pedido crearPedidoDesdeCarrito(HttpSession session) {

        List<ItemCarrito> carrito =
                carritoService.obtenerCarrito(session);

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede confirmar un pedido con el carrito vacío."
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {

            throw new IllegalStateException(
                    "Debe iniciar sesión para confirmar el pedido."
            );
        }

        String email = authentication.getName();

        Usuario usuario = usuarioRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el usuario autenticado."
                        )
                );

        Cliente cliente = clienteRepository
                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El usuario autenticado no tiene un cliente asociado."
                        )
                );

        var estadoPendiente = estadoPedidoRepository
                .findByNombre("Pendiente")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el estado Pendiente."
                        )
                );

        /*
         * Validar todo antes de guardar.
         */
        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarrito item : carrito) {

            Repuesto repuesto = repuestoRepository
                    .findById(item.getIdRepuesto())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "El repuesto "
                                    + item.getNombre()
                                    + " ya no existe."
                            )
                    );

            Inventario inventario = inventarioRepository
                    .findByRepuesto_IdRepuesto(
                            repuesto.getIdRepuesto()
                    )
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No existe inventario para "
                                    + repuesto.getNombre()
                                    + "."
                            )
                    );

            if (item.getCantidad() == null
                    || item.getCantidad() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad de "
                        + repuesto.getNombre()
                        + " no es válida."
                );
            }

            if (inventario.getCantidadActual()
                    < item.getCantidad()) {

                throw new IllegalArgumentException(
                        "No hay suficiente stock de "
                        + repuesto.getNombre()
                        + ". Disponible: "
                        + inventario.getCantidadActual()
                );
            }

            BigDecimal subtotal =
                    repuesto.getPrecio()
                            .multiply(
                                    BigDecimal.valueOf(
                                            item.getCantidad()
                                    )
                            );

            total = total.add(subtotal);
        }

        /*
         * Crear pedido.
         */
        Pedido pedido = new Pedido();

        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(total);
        pedido.setCliente(cliente);
        pedido.setEstadoPedido(estadoPendiente);

        Pedido pedidoGuardado =
                pedidoRepository.save(pedido);

        /*
         * Crear detalles y descontar inventario.
         */
        for (ItemCarrito item : carrito) {

            Repuesto repuesto = repuestoRepository
                    .findById(item.getIdRepuesto())
                    .orElseThrow();

            Inventario inventario = inventarioRepository
                    .findByRepuesto_IdRepuesto(
                            repuesto.getIdRepuesto()
                    )
                    .orElseThrow();

            BigDecimal precioUnitario =
                    repuesto.getPrecio();

            BigDecimal subtotal =
                    precioUnitario.multiply(
                            BigDecimal.valueOf(
                                    item.getCantidad()
                            )
                    );

            DetallePedido detalle =
                    new DetallePedido();

            detalle.setPedido(pedidoGuardado);
            detalle.setRepuesto(repuesto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            detallePedidoRepository.save(detalle);

            int nuevoStock =
                    inventario.getCantidadActual()
                    - item.getCantidad();

            inventario.setCantidadActual(nuevoStock);

            inventarioRepository.save(inventario);

            /*
             * Mantener repuesto.stock sincronizado.
             */
            repuesto.setStock(nuevoStock);

            repuestoRepository.save(repuesto);
        }

        return pedidoGuardado;
    }

    /*
     * =========================================================
     * CANCELACIÓN REAL DEL PEDIDO POR PARTE DEL CLIENTE
     * =========================================================
     */
    @Transactional
    public void cancelarPedidoCliente(
            Integer idPedido,
            String emailUsuario) {

        Pedido pedido = pedidoRepository
                .findById(idPedido)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no existe."
                        )
                );

        /*
         * Verificar que el pedido pertenece al usuario.
         */
        String emailPropietario =
                pedido.getCliente()
                        .getUsuario()
                        .getEmail();

        if (!emailPropietario.equalsIgnoreCase(emailUsuario)) {

            throw new IllegalArgumentException(
                    "No tiene permiso para cancelar este pedido."
            );
        }

        cancelarPedidoInterno(pedido);
    }

    /*
     * =========================================================
     * CAMBIO DE ESTADO POR ADMINISTRADOR / VENTAS
     * =========================================================
     */
    @Transactional
    public void cambiarEstado(
            Integer idPedido,
            String nombreEstado) {

        Pedido pedido = pedidoRepository
                .findById(idPedido)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pedido no encontrado: "
                                + idPedido
                        )
                );

        /*
         * Si desde Gestión de Pedidos se selecciona Cancelado,
         * también hay que devolver el inventario.
         */
        if ("Cancelado".equalsIgnoreCase(nombreEstado)) {

            cancelarPedidoInterno(pedido);
            return;
        }

        /*
         * Un pedido cancelado no puede volver a activarse.
         */
        if ("Cancelado".equalsIgnoreCase(
                pedido.getEstadoPedido().getNombre())) {

            throw new IllegalArgumentException(
                    "Un pedido cancelado no puede cambiar de estado."
            );
        }

        var estado = estadoPedidoRepository
                .findByNombre(nombreEstado)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Estado no válido: "
                                + nombreEstado
                        )
                );

        pedido.setEstadoPedido(estado);

        pedidoRepository.save(pedido);
    }

    /*
     * =========================================================
     * LÓGICA INTERNA DE CANCELACIÓN
     * =========================================================
     */
    private void cancelarPedidoInterno(Pedido pedido) {

        String estadoActual =
                pedido.getEstadoPedido().getNombre();

        /*
         * Evita devolver el inventario dos veces.
         */
        if ("Cancelado".equalsIgnoreCase(estadoActual)) {

            throw new IllegalArgumentException(
                    "El pedido ya se encuentra cancelado."
            );
        }

        /*
         * Cliente solo debería cancelar pedidos pendientes.
         * También protegemos la lógica aquí para mantener
         * consistencia.
         */
        if (!"Pendiente".equalsIgnoreCase(estadoActual)) {

            throw new IllegalArgumentException(
                    "Solo se pueden cancelar pedidos pendientes."
            );
        }

        List<DetallePedido> detalles =
                detallePedidoRepository
                        .findByPedido_IdPedido(
                                pedido.getIdPedido()
                        );

        /*
         * Devolver cada producto al inventario.
         */
        for (DetallePedido detalle : detalles) {

            Repuesto repuesto =
                    detalle.getRepuesto();

            Inventario inventario =
                    inventarioRepository
                            .findByRepuesto_IdRepuesto(
                                    repuesto.getIdRepuesto()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "No existe inventario para "
                                            + repuesto.getNombre()
                                            + "."
                                    )
                            );

            int stockActual =
                    inventario.getCantidadActual();

            int cantidadDevuelta =
                    detalle.getCantidad();

            int nuevoStock =
                    stockActual + cantidadDevuelta;

            /*
             * Actualizar inventario.
             */
            inventario.setCantidadActual(nuevoStock);

            inventarioRepository.save(inventario);

            /*
             * Mantener repuesto.stock sincronizado.
             */
            repuesto.setStock(nuevoStock);

            repuestoRepository.save(repuesto);
        }

        var estadoCancelado = estadoPedidoRepository
                .findByNombre("Cancelado")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el estado Cancelado."
                        )
                );

        pedido.setEstadoPedido(estadoCancelado);

        pedidoRepository.save(pedido);
    }
}