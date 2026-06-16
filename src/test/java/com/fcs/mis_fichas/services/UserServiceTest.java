package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.UserResponse;
import com.fcs.mis_fichas.dtos.UserUpdateRequest;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void findById_shouldReturnUserResponse_whenUserExists() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user@example.com");
    }

    @Test
    void findById_shouldThrowIllegalArgumentException_whenUserNotFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 1");
    }

    @Test
    void findAll_shouldReturnAllUsers_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).email("user@example.com").name("User").role(Role.USER).status(Status.ACTIVE).build();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);

        Page<UserResponse> result = userService.findAll(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("user@example.com");
    }

    @Test
    void findAll_shouldReturnFilteredByRole_whenRoleProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).email("admin@example.com").name("Admin").role(Role.ADMIN).status(Status.ACTIVE).build();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByDeletedAtIsNullAndRole(Role.ADMIN, pageable)).thenReturn(page);

        Page<UserResponse> result = userService.findAll(Role.ADMIN, null, pageable);

        assertThat(result.getContent().get(0).role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void findAll_shouldReturnFilteredByStatus_whenStatusProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).email("user@example.com").name("User").role(Role.USER).status(Status.BLOCKED).build();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByDeletedAtIsNullAndStatus(Status.BLOCKED, pageable)).thenReturn(page);

        Page<UserResponse> result = userService.findAll(null, Status.BLOCKED, pageable);

        assertThat(result.getContent().get(0).status()).isEqualTo(Status.BLOCKED);
    }

    @Test
    void findAll_shouldReturnFilteredByRoleAndStatus_whenBothProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = User.builder().id(1L).email("user@example.com").name("User").role(Role.USER).status(Status.ACTIVE).build();
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findByDeletedAtIsNullAndRoleAndStatus(Role.USER, Status.ACTIVE, pageable)).thenReturn(page);

        Page<UserResponse> result = userService.findAll(Role.USER, Status.ACTIVE, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_shouldModifyUser_whenEmailNotDuplicated() {
        User user = User.builder()
                .id(1L)
                .email("old@example.com")
                .name("Old Name")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        UserUpdateRequest request = new UserUpdateRequest("New Name", "new@example.com", Role.ADMIN, Status.BLOCKED);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndDeletedAtIsNullAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(response.status()).isEqualTo(Status.BLOCKED);
    }

    @Test
    void update_shouldThrowIllegalArgumentException_whenEmailAlreadyInUse() {
        User user = User.builder()
                .id(1L)
                .email("old@example.com")
                .name("Old Name")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        UserUpdateRequest request = new UserUpdateRequest("New Name", "new@example.com", Role.USER, Status.ACTIVE);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndDeletedAtIsNullAndIdNot("new@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void update_shouldSucceed_whenEmailUnchanged() {
        User user = User.builder()
                .id(1L)
                .email("same@example.com")
                .name("Old Name")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
        UserUpdateRequest request = new UserUpdateRequest("New Name", "same@example.com", Role.USER, Status.ACTIVE);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        verify(userRepository, never()).existsByEmailAndDeletedAtIsNullAndIdNot(any(), any());
    }

    @Test
    void delete_shouldSoftDeleteUser() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("User")
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.delete(1L);

        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldThrowIllegalArgumentException_whenUserNotFound() {
        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 1");
    }
}
