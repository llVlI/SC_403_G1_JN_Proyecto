package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.Rol;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.repository.ClienteRepository;
import com.autopartescr.repuestos.repository.RolRepository;
import com.autopartescr.repuestos.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> login(String email, String password) {
        return usuarioRepository.findByEmailAndPassword(email, password);
    }

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }
    
    @Transactional(readOnly = true)
public Cliente obtenerClientePorUsuario(Integer idUsuario) {
    return clienteRepository.findByUsuario_IdUsuario(idUsuario).orElse(null);
}

    // Todo registro publico (HU-09) crea un usuario con rol CLIENTE
    @Transactional
    public Cliente registrarCliente(Cliente cliente) {
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException(
                        "El rol CLIENTE no existe. Ejecutar el script sql/script.sql primero."));

        Usuario usuario = cliente.getUsuario();
        usuario.setRol(rolCliente);
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        cliente.setUsuario(usuarioGuardado);
        return clienteRepository.save(cliente);
    }
}
