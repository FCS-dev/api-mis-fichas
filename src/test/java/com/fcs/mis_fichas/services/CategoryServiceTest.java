package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.CategoryRequest;
import com.fcs.mis_fichas.dtos.CategoryResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.TransactionRepository;
import com.fcs.mis_fichas.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, transactionRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String email, Role role) {
        User user = User.builder().id(1L).email(email).name("Test").role(role).status(Status.ACTIVE).build();
        lenient().when(authentication.getName()).thenReturn(email);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
    }

    @Test
    void create_shouldCreateCategory_whenNameNotExists() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        CategoryRequest request = new CategoryRequest("Food", Type.EXPENSE);

        when(categoryRepository.existsByNameAndDeletedAtIsNull("Food")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryResponse response = categoryService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Food");
        assertThat(response.type()).isEqualTo(Type.EXPENSE);
        assertThat(response.createdById()).isEqualTo(1L);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenNameAlreadyExists() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        CategoryRequest request = new CategoryRequest("Food", Type.EXPENSE);

        when(categoryRepository.existsByNameAndDeletedAtIsNull("Food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name already exists");
    }

    @Test
    void update_shouldModifyCategory_whenNameNotDuplicated() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category existing = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        CategoryRequest request = new CategoryRequest("Groceries", Type.EXPENSE);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndDeletedAtIsNull("Groceries")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("Groceries");
    }

    @Test
    void update_shouldThrow_whenCategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        CategoryRequest request = new CategoryRequest("Food", Type.EXPENSE);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with id: 1");
    }

    @Test
    void update_shouldThrow_whenNameAlreadyExists() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category existing = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        CategoryRequest request = new CategoryRequest("Travel", Type.EXPENSE);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameAndDeletedAtIsNull("Travel")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category name already exists");
    }

    @Test
    void delete_shouldSoftDeleteCategory() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category existing = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        categoryService.delete(1L);

        assertThat(existing.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldThrow_whenCategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with id: 1");
    }

    @Test
    void findById_shouldReturnCategory_whenExists() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category existing = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));

        CategoryResponse response = categoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Food");
    }

    @Test
    void findById_shouldThrow_whenCategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with id: 1");
    }

    @Test
    void findAll_shouldReturnPageOfCategories() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Pageable pageable = PageRequest.of(0, 10);
        Category cat = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Page<Category> page = new PageImpl<>(List.of(cat));

        when(categoryRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);

        Page<CategoryResponse> result = categoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Food");
    }
}
