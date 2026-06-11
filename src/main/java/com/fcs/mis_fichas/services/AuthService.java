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
 * Servicio de autenticacion y autorizacion.
 * Gestiona el registro de usuarios, inicio de sesion, refresco de tokens y cierre de sesion.
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
     * La contrasena se almacena hasheada con BCrypt.
     *
     * @param request datos de registro del usuario
     * @return entidad User del usuario creado
     * @throws RuntimeException si el correo ya esta registrado
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already registered");
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
     * Inicia sesion de un usuario.
     * Valida las credenciales, verifica que la cuenta este activa,
     * revoca todos los tokens previos y genera nuevos tokens de acceso y refresco.
     *
     * @param request datos de inicio de sesion (email y password)
     * @return DTO con el access token y refresh token
     * @throws BadCredentialsException si las credenciales son invalidas
     * @throws RuntimeException        si la cuenta del usuario no esta activa
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.getStatus().equals(Status.ACTIVE)) {
            throw new RuntimeException("User account is not active");
        }

        refreshTokenService.revokeAllUserTokens(user.getId());
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Refresca el access token usando un refresh token valido.
     * Aplica rotacion de refresh tokens: revoca el anterior y genera uno nuevo.
     *
     * @param refreshToken token de refresco actual
     * @return DTO con el nuevo access token y el nuevo refresh token
     * @throws RuntimeException si el refresh token no es valido o no se puede rotar
     */
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        RefreshTokenService refreshTokenService = this.refreshTokenService;
        var tokenEntity = refreshTokenService.findByTokenHash(newRefreshToken)
                .orElseThrow(() -> new RuntimeException("Failed to create new refresh token"));
        
        User user = tokenEntity.getUserId();
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        
        return new AuthResponse(accessToken, newRefreshToken);
    }

    /**
     * Cierra la sesion del usuario revocando el refresh token.
     *
     * @param refreshToken token de refresco a revocar
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
