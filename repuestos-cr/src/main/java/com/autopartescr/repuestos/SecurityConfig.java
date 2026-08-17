package com.autopartescr.repuestos;

import com.autopartescr.repuestos.domain.Ruta;
import com.autopartescr.repuestos.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Configuracion central de seguridad de AutoPartes CR.
//
// En vez de escribir las reglas de acceso directamente aqui, se leen de
// la tabla "ruta" en la base de datos (ver RutaService y Ruta). Asi, si
// el equipo agrega un modulo nuevo, solo hace falta un INSERT en la
// tabla ruta, sin tocar este archivo.
@Configuration
public class SecurityConfig {

    // ADMINISTRADOR hereda todos los permisos de ENCARGADO_VENTAS.
    // Asi, cualquier ruta protegida para ENCARGADO_VENTAS tambien la
    // puede usar el ADMINISTRADOR, sin tener que listar los dos roles
    // en cada fila de la tabla ruta.
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy(
                "ROLE_ADMINISTRADOR > ROLE_ENCARGADO_VENTAS"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, @Lazy RutaService rutaService)
            throws Exception {

        var rutas = rutaService.getRutas();

        http.authorizeHttpRequests(requests -> {
            for (Ruta ruta : rutas) {
                if (ruta.isRequiereRol()) {
                    requests.requestMatchers(ruta.getRuta()).hasRole(ruta.getRol().getNombre());
                } else {
                    requests.requestMatchers(ruta.getRuta()).permitAll();
                }
            }
            // Los recursos estaticos y webjars (bootstrap, jquery, etc.)
            // siempre deben quedar publicos, sin importar la tabla ruta.
            requests.requestMatchers("/webjars/**", "/css/**", "/js/**", "/static/**").permitAll();
            requests.anyRequest().authenticated();
        });

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
        ).logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        ).exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/acceso-denegado")
        ).sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configurarGlobal(AuthenticationManagerBuilder build,
            @Lazy PasswordEncoder passwordEncoder,
            @Lazy UserDetailsService userDetailsService) throws Exception {
        build.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
