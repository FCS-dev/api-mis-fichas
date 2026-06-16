package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.SubcategoryRequest;
import com.fcs.mis_fichas.dtos.SubcategoryResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
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
class SubcategoryServiceTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private SubcategoryService subcategoryService;

    @BeforeEach
    void setUp() {
        subcategoryService = new SubcategoryService(subcategoryRepository, categoryRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User mockAuthenticatedUser(String email, Role role) {
        User user = User.builder().id(1L).email(email).name("Test").role(role).status(Status.ACTIVE).build();
        when(authentication.getName()).thenReturn(email);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmailAndDeletedAtIsNull(email)).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void create_shouldCreateSystemSubcategory_whenAdmin() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, "Comments");

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull("Groceries", 1L)).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> {
            Subcategory s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SubcategoryResponse response = subcategoryService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.isSystem()).isTrue();
        assertThat(response.categoryId()).isEqualTo(1L);
    }

    @Test
    void create_shouldCreateUserSubcategory_whenUser() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, "Comments");

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull("Groceries", 1L)).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> {
            Subcategory s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SubcategoryResponse response = subcategoryService.create(request);

        assertThat(response.isSystem()).isFalse();
    }

    @Test
    void create_shouldThrow_whenCategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, null);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subcategoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with id: 1");
    }

    @Test
    void create_shouldThrow_whenDuplicateName() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, null);

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull("Groceries", 1L)).thenReturn(true);

        assertThatThrownBy(() -> subcategoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subcategory name already exists in this category");
    }

    @Test
    void update_shouldAllowAdminToModifyAnySubcategory() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Old")
                .category(category)
                .isSystem(true)
                .createdBy(User.builder().id(2L).build())
                .build();
        SubcategoryRequest request = new SubcategoryRequest("New", 1L, "Updated");

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull("New", 1L)).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        SubcategoryResponse response = subcategoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("New");
    }

    @Test
    void update_shouldAllowUserToModifyOwnSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Old")
                .category(category)
                .isSystem(false)
                .createdBy(user)
                .build();
        SubcategoryRequest request = new SubcategoryRequest("New", 1L, "Updated");

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryIdAndDeletedAtIsNull("New", 1L)).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        SubcategoryResponse response = subcategoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("New");
    }

    @Test
    void update_shouldThrow_whenUserTriesToModifySystemSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("System")
                .category(category)
                .isSystem(true)
                .createdBy(User.builder().id(2L).build())
                .build();
        SubcategoryRequest request = new SubcategoryRequest("New", 1L, "Updated");

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        assertThatThrownBy(() -> subcategoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("System subcategories can only be modified by an ADMIN");
    }

    @Test
    void update_shouldThrow_whenUserTriesToModifyAnotherUsersSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Other")
                .category(category)
                .isSystem(false)
                .createdBy(User.builder().id(2L).build())
                .build();
        SubcategoryRequest request = new SubcategoryRequest("New", 1L, "Updated");

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        assertThatThrownBy(() -> subcategoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only modify your own subcategories");
    }

    @Test
    void delete_shouldAllowAdminToDeleteAnySubcategory() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("System")
                .isSystem(true)
                .createdBy(User.builder().id(2L).build())
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        subcategoryService.delete(1L);

        assertThat(subcategory.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldAllowUserToDeleteOwnSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Mine")
                .isSystem(false)
                .createdBy(user)
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(inv -> inv.getArgument(0));

        subcategoryService.delete(1L);

        assertThat(subcategory.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldThrow_whenUserTriesToDeleteSystemSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("System")
                .isSystem(true)
                .createdBy(User.builder().id(2L).build())
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        assertThatThrownBy(() -> subcategoryService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("System subcategories can only be modified by an ADMIN");
    }

    @Test
    void findById_shouldAllowAdminToViewAnySubcategory() {
        mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Private")
                .isSystem(false)
                .createdBy(User.builder().id(2L).build())
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        SubcategoryResponse response = subcategoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldAllowUserToViewSystemSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("System")
                .isSystem(true)
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        SubcategoryResponse response = subcategoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldAllowUserToViewOwnSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Mine")
                .isSystem(false)
                .createdBy(user)
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        SubcategoryResponse response = subcategoryService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrow_whenUserTriesToViewAnotherUsersSubcategory() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Subcategory subcategory = Subcategory.builder()
                .id(1L)
                .name("Other")
                .isSystem(false)
                .createdBy(User.builder().id(2L).build())
                .build();

        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        assertThatThrownBy(() -> subcategoryService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You do not have permission to view this subcategory");
    }

    @Test
    void findAll_shouldReturnAllSubcategories_whenAdmin() {
        mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Pageable pageable = PageRequest.of(0, 10);
        Subcategory sub = Subcategory.builder().id(1L).name("Sub").isSystem(false).build();
        Page<Subcategory> page = new PageImpl<>(List.of(sub));

        when(subcategoryRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);

        Page<SubcategoryResponse> result = subcategoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_shouldReturnAccessibleSubcategories_whenUser() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Pageable pageable = PageRequest.of(0, 10);
        Subcategory sub = Subcategory.builder().id(1L).name("Sub").isSystem(true).build();
        Page<Subcategory> page = new PageImpl<>(List.of(sub));

        when(subcategoryRepository.findByDeletedAtIsNullAndAccessibleToUser(user.getId(), pageable)).thenReturn(page);

        Page<SubcategoryResponse> result = subcategoryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
