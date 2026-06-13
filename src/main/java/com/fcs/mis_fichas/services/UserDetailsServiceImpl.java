package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementación de {@link UserDetailsService} para Spring Security.
 * Carga los detalles del usuario desde la base de datos usando su correo electrónico.
 * Asigna el rol del usuario como autoridad (ROLE_USER o ROLE_ADMIN).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carga un usuario por su correo electrónico.
     * Construye un objeto UserDetails con el email, hash de contraseña, estado de la cuenta
     * y el rol como autoridad.
     *
     * @param email correo electrónico del usuario
     * @return detalles del usuario para Spring Security
     * @throws UsernameNotFoundException si no se encuentra un usuario con ese correo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus().name().equals("ACTIVE"),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
