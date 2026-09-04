package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.AuthResponse;
import com.fcs.mis_fichas.dtos.LoginRequest;
import com.fcs.mis_fichas.dtos.RegisterRequest;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, refreshTokenService);
    }

    @Test
    void register_shouldCreateUser_whenEmailNotRegistered() {
        RegisterRequest request = new RegisterRequest("new@example.com", "password123", "New User");

        when(userRepository.findByEmailAndDeletedAtIsNull("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = authService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void register_shouldThrowIllegalArgumentException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "Existing User");
        User existing = User.builder().email("existing@example.com").build();

        when(userRepository.findByEmailAndDeletedAtIsNull("existing@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .passwordHash("encoded")
                .name("User")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("user@example.com", "USER", "User", 1L)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenService).revokeAllUserTokens(1L);
    }

    @Test
    void login_shouldThrowBadCredentialsException_whenUserNotFound() {
        LoginRequest request = new LoginRequest("missing@example.com", "password123");
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmailAndDeletedAtIsNull("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_shouldThrowIllegalArgumentException_whenUserNotActive() {
        LoginRequest request = new LoginRequest("blocked@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("blocked@example.com")
                .passwordHash("encoded")
                .name("Blocked")
                .role(Role.USER)
                .status(Status.BLOCKED)
                .build();
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByEmailAndDeletedAtIsNull("blocked@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User account is not active");
    }

    @Test
    void refresh_shouldReturnNewAuthResponse() {
        com.fcs.mis_fichas.entities.RefreshToken tokenEntity = com.fcs.mis_fichas.entities.RefreshToken.builder()
                .id(1L)
                .user(User.builder().id(1L).email("user@example.com").role(Role.USER).build())
                .build();

        when(refreshTokenService.rotateRefreshToken("old-refresh")).thenReturn("new-refresh");
        when(refreshTokenService.findByTokenHash("new-refresh")).thenReturn(Optional.of(tokenEntity));
        when(jwtService.generateAccessToken("user@example.com", "USER", "User", 1L)).thenReturn("new-access");

        AuthResponse response = authService.refresh("old-refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refresh_shouldThrowIllegalArgumentException_whenNewTokenNotFound() {
        when(refreshTokenService.rotateRefreshToken("old-refresh")).thenReturn("new-refresh");
        when(refreshTokenService.findByTokenHash("new-refresh")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("old-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to create new refresh token");
    }

    @Test
    void logout_shouldRevokeRefreshToken() {
        authService.logout("refresh-token");
        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }
}
