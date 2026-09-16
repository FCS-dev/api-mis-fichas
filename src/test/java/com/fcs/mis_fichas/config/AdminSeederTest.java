package com.fcs.mis_fichas.config;

import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SubcategoryRepository subcategoryRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private org.springframework.core.env.Environment environment;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private AdminSeeder adminSeeder;

    @BeforeEach
    void setUp() {
        adminSeeder = new AdminSeeder(userRepository, categoryRepository, subcategoryRepository, passwordEncoder, environment, jdbcTemplate);
        ReflectionTestUtils.setField(adminSeeder, "adminEmail", "admin@mis-fichas.fcs");
        ReflectionTestUtils.setField(adminSeeder, "adminPassword", "secret-password");
    }

    @Test
    void seedAdmin_shouldCreateAdmin_whenNotExists() throws Exception {
        when(userRepository.findByEmailAndDeletedAtIsNull("admin@mis-fichas.fcs")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.findByNameAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(subcategoryRepository.findByNameAndCategoryIdAndDeletedAtIsNull(any(), any())).thenReturn(Optional.empty());
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        var runner = adminSeeder.seedAdmin();
        runner.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedAdmin = userCaptor.getValue();
        assertThat(savedAdmin.getEmail()).isEqualTo("admin@mis-fichas.fcs");
        assertThat(savedAdmin.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedAdmin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedAdmin.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void seedAdmin_shouldNotCreateAdmin_whenAlreadyExists() throws Exception {
        User existingAdmin = User.builder()
                .id(1L)
                .email("admin@mis-fichas.fcs")
                .passwordHash("existing-hash")
                .name("Existing")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        when(userRepository.findByEmailAndDeletedAtIsNull("admin@mis-fichas.fcs")).thenReturn(Optional.of(existingAdmin));

        var runner = adminSeeder.seedAdmin();
        runner.run();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void seedAdmin_shouldCreateCategoriesAndSubcategories_whenNotExist() throws Exception {
        when(userRepository.findByEmailAndDeletedAtIsNull("admin@mis-fichas.fcs")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryRepository.findByNameAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(subcategoryRepository.findByNameAndCategoryIdAndDeletedAtIsNull(any(), any())).thenReturn(Optional.empty());
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        var runner = adminSeeder.seedAdmin();
        runner.run();

        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
        verify(subcategoryRepository, atLeastOnce()).save(any(Subcategory.class));
    }

    @Test
    void seedAdmin_shouldNotCreateAnything_whenAdminAlreadyExists() throws Exception {
        User existingAdmin = User.builder()
                .id(1L)
                .email("admin@mis-fichas.fcs")
                .passwordHash("hash")
                .name("Admin")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        when(userRepository.findByEmailAndDeletedAtIsNull("admin@mis-fichas.fcs")).thenReturn(Optional.of(existingAdmin));

        var runner = adminSeeder.seedAdmin();
        runner.run();

        verify(userRepository, never()).save(any(User.class));
        verify(categoryRepository, never()).save(any(Category.class));
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
        verify(jdbcTemplate, never()).execute(anyString());
    }
}
