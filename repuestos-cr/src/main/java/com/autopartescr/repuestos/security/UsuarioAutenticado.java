package com.autopartescr.repuestos.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

// Extiende el User de Spring Security para poder llevar el nombre real
// del usuario (no solo el email) junto con la sesion. Asi el menu
// puede mostrar "Hola, Juan Perez" en vez de "Hola, juan@correo.com".
public class UsuarioAutenticado extends User {

    private final String nombre;

    public UsuarioAutenticado(String email, String password,
                               Collection<? extends GrantedAuthority> authorities,
                               String nombre) {
        super(email, password, authorities);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
