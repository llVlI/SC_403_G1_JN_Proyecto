package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Cliente;
import com.autopartescr.repuestos.domain.Rol;
import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.repository.ClienteRepository;
import com.autopartescr.repuestos.repository.RolRepository;
import com.autopartescr.repuestos.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // El login en si ya no lo maneja este service: Spring Security lo
    // hace a traves de UsuarioDetailsService y el PasswordEncoder.

    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public Cliente obtenerClientePorUsuario(Integer idUsuario) {
        return clienteRepository.findByUsuario_IdUsuario(idUsuario).orElse(null);
    }

    // Todo registro publico (HU-09) crea un usuario con rol CLIENTE.
    // La contrasena se guarda con BCrypt, nunca en texto plano.
    @Transactional
    public Cliente registrarCliente(Cliente cliente) {
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new IllegalStateException(
                        "El rol CLIENTE no existe. Ejecutar el script sql/script.sql primero."));

        Usuario usuario = cliente.getUsuario();
        usuario.setRol(rolCliente);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        cliente.setUsuario(usuarioGuardado);
        return clienteRepository.save(cliente);
    }
}
