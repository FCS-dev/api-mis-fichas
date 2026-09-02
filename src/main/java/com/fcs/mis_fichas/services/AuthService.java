package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.AuthResponse;
import com.fcs.mis_fichas.dtos.LoginRequest;
import com.fcs.mis_fichas.dtos.RegisterRequest;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación y autorización.
 * Gestiona el registro de usuarios, inicio de sesión, refresco de tokens y cierre de sesión.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Registra un nuevo usuario en el sistema.
     * El rol asignado siempre es USER. La cuenta se crea con estado ACTIVE.
     * La contraseña se almacena hasheada con BCrypt.
     *
     * @param request datos de registro del usuario
     * @return entidad User del usuario creado
     * @throws RuntimeException si el correo ya está registrado
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmailAndDeletedAtIsNull(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    /**
     * Inicia sesión de un usuario.
     * Valida las credenciales, verifica que la cuenta este activa,
     * revoca todos los tokens previos y genera nuevos tokens de acceso y refresco.
     *
     * @param request datos de inicio de sesión (email y password)
     * @return DTO con el access token y refresh token
     * @throws BadCredentialsException si las credenciales son inválidas
     * @throws RuntimeException        si la cuenta del usuario no está activa
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.getStatus().equals(Status.ACTIVE)) {
            throw new IllegalArgumentException("User account is not active");
        }

        refreshTokenService.revokeAllUserTokens(user.getId());
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getName());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Refresca el access token usando un refresh token válido.
     * Aplica rotación de refresh tokens: revoca el anterior y genera uno nuevo.
     *
     * @param refreshToken token de refresco actual
     * @return DTO con el nuevo access token y el nuevo refresh token
     * @throws RuntimeException si el refresh token no es válido o no se puede rotar
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        var tokenEntity = refreshTokenService.findByTokenHash(newRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Failed to create new refresh token"));

        User user = tokenEntity.getUser();
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name(), user.getName());

        return new AuthResponse(accessToken, newRefreshToken);
    }

    /**
     * Cierra la sesión del usuario revocando el refresh token.
     *
     * @param refreshToken token de refresco a revocar
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
