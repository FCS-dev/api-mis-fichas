package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.entities.RefreshToken;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", 2592000000L); // 30 days
    }

    @Test
    void createRefreshToken_shouldReturnPlainToken_andSaveHash() {
        User user = User.builder().id(1L).email("user@example.com").build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String token = refreshTokenService.createRefreshToken(user);

        assertThat(token).isNotNull().isNotBlank();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getTokenHash()).isNotNull();
        assertThat(captor.getValue().getRevokedAt()).isNull();
    }

    @Test
    void findByTokenHash_shouldReturnToken_whenExists() {
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .tokenHash("someHash")
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByTokenHash("plainToken");

        assertThat(result).isPresent();
    }

    @Test
    void rotateRefreshToken_shouldReturnNewToken_whenOldValid() {
        User user = User.builder().id(1L).email("user@example.com").build();
        RefreshToken oldToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash("oldHash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revokedAt(null)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String newToken = refreshTokenService.rotateRefreshToken("oldPlainToken");

        assertThat(newToken).isNotNull().isNotBlank().isNotEqualTo("oldPlainToken");
        assertThat(oldToken.getRevokedAt()).isNotNull();
    }

    @Test
    void rotateRefreshToken_shouldThrow_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    void rotateRefreshToken_shouldRevokeAllAndThrow_whenTokenAlreadyRevoked() {
        User user = User.builder().id(1L).email("user@example.com").build();
        RefreshToken oldToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash("oldHash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revokedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.findByUserIdAndRevokedAtIsNull(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("oldPlainToken"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token was revoked");

        verify(refreshTokenRepository).findByUserIdAndRevokedAtIsNull(1L);
    }

    @Test
    void rotateRefreshToken_shouldThrow_whenTokenExpired() {
        User user = User.builder().id(1L).email("user@example.com").build();
        RefreshToken oldToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenHash("oldHash")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .revokedAt(null)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(oldToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("oldPlainToken"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token expired");
    }

    @Test
    void revokeRefreshToken_shouldSetRevokedAt() {
        User user = User.builder().id(1L).email("user@example.com").build();
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .tokenHash("hash")
                .revokedAt(null)
                .user(user)
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.revokeRefreshToken("plainToken");

        assertThat(token.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeAllUserTokens_shouldRevokeAllActiveTokens() {
        RefreshToken t1 = RefreshToken.builder().id(1L).revokedAt(null).build();
        RefreshToken t2 = RefreshToken.builder().id(2L).revokedAt(null).build();

        when(refreshTokenRepository.findByUserIdAndRevokedAtIsNull(1L)).thenReturn(List.of(t1, t2));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        refreshTokenService.revokeAllUserTokens(1L);

        assertThat(t1.getRevokedAt()).isNotNull();
        assertThat(t2.getRevokedAt()).isNotNull();
    }
}
