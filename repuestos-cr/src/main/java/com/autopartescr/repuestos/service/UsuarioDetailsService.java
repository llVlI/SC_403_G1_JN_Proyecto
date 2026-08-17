package com.autopartescr.repuestos.service;

import com.autopartescr.repuestos.domain.Usuario;
import com.autopartescr.repuestos.repository.UsuarioRepository;
import com.autopartescr.repuestos.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Puente entre nuestra tabla "usuario" y Spring Security.
// Aqui se inicia sesion con el email (nuestro "username" es el email),
// y el unico rol del usuario se convierte al formato que Spring Security
// espera: "ROLE_NOMBRE" (por ejemplo ROLE_ADMINISTRADOR).
@Service("userDetailsService")
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        var authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre());

        return new UsuarioAutenticado(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(authority),
                usuario.getNombre()
        );
    }
}
