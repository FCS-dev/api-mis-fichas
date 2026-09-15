package com.fcs.mis_fichas.services;

import com.fcs.mis_fichas.dtos.TransactionRequest;
import com.fcs.mis_fichas.dtos.TransactionResponse;
import com.fcs.mis_fichas.entities.Category;
import com.fcs.mis_fichas.entities.Subcategory;
import com.fcs.mis_fichas.entities.Transaction;
import com.fcs.mis_fichas.entities.User;
import com.fcs.mis_fichas.enums.Role;
import com.fcs.mis_fichas.enums.Status;
import com.fcs.mis_fichas.enums.Type;
import com.fcs.mis_fichas.repositories.CategoryRepository;
import com.fcs.mis_fichas.repositories.SubcategoryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SubcategoryRepository subcategoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, categoryRepository, subcategoryRepository, userRepository);
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
    void create_shouldCreateTransaction_whenUser() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(category).isSystem(true).build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("100.00"), "Lunch", LocalDate.now());

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponse response = transactionService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.amount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void create_shouldThrow_whenAdmin() {
        mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("100.00"), "Lunch", LocalDate.now());

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN cannot register own transactions");
    }

    @Test
    void create_shouldThrow_whenCategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("100.00"), "Lunch", LocalDate.now());

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found or is deleted");
    }

    @Test
    void create_shouldThrow_whenSubcategoryNotFound() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("100.00"), "Lunch", LocalDate.now());

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subcategory not found or is deleted");
    }

    @Test
    void create_shouldThrow_whenSubcategoryDoesNotBelongToCategory() {
        mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Category otherCategory = Category.builder().id(2L).name("Travel").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(otherCategory).isSystem(true).build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("100.00"), "Lunch", LocalDate.now());

        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));

        assertThatThrownBy(() -> transactionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subcategory does not belong to the specified category");
    }

    @Test
    void update_shouldAllowUserToModifyOwnTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(category).isSystem(true).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .category(category)
                .subcategory(subcategory)
                .amount(new BigDecimal("50.00"))
                .description("Old")
                .transactionDate(LocalDate.now().minusDays(1))
                .build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("75.00"), "Updated", LocalDate.now());

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.update(1L, request);

        assertThat(response.amount()).isEqualTo(new BigDecimal("75.00"));
        assertThat(response.description()).isEqualTo("Updated");
    }

    @Test
    void update_shouldAllowAdminToModifyUserTransaction() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        User user = User.builder().id(2L).email("user@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(category).isSystem(true).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .category(category)
                .subcategory(subcategory)
                .amount(new BigDecimal("50.00"))
                .description("Old")
                .transactionDate(LocalDate.now().minusDays(1))
                .build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("75.00"), "Updated", LocalDate.now());

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(subcategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.update(1L, request);

        assertThat(response.amount()).isEqualTo(new BigDecimal("75.00"));
    }

    @Test
    void update_shouldThrow_whenUserTriesToModifyOthersTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        User other = User.builder().id(2L).email("other@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(category).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(other)
                .category(category)
                .subcategory(subcategory)
                .amount(new BigDecimal("50.00"))
                .description("Other")
                .transactionDate(LocalDate.now().minusDays(1))
                .build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("75.00"), "Updated", LocalDate.now());

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only modify your own transactions");
    }

    @Test
    void update_shouldThrow_whenAdminTriesToModifyAdminTransaction() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        User otherAdmin = User.builder().id(2L).email("otherAdmin@example.com").role(Role.ADMIN).status(Status.ACTIVE).build();
        Category category = Category.builder().id(1L).name("Food").type(Type.EXPENSE).build();
        Subcategory subcategory = Subcategory.builder().id(1L).name("Groceries").category(category).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(otherAdmin)
                .category(category)
                .subcategory(subcategory)
                .amount(new BigDecimal("50.00"))
                .description("Admin")
                .transactionDate(LocalDate.now().minusDays(1))
                .build();
        TransactionRequest request = new TransactionRequest(null, 1L, 1L, new BigDecimal("75.00"), "Updated", LocalDate.now());

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN can only modify transactions of USER accounts");
    }

    @Test
    void delete_shouldAllowUserToDeleteOwnTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.delete(1L);

        assertThat(transaction.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldAllowAdminToDeleteUserTransaction() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        User user = User.builder().id(2L).email("user@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.delete(1L);

        assertThat(transaction.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_shouldThrow_whenUserTriesToDeleteOthersTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        User other = User.builder().id(2L).email("other@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(other)
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You can only delete your own transactions");
    }

    @Test
    void delete_shouldThrow_whenAdminTriesToDeleteAdminTransaction() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        User otherAdmin = User.builder().id(2L).email("otherAdmin@example.com").role(Role.ADMIN).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(otherAdmin)
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN can only delete transactions of USER accounts");
    }

    @Test
    void findById_shouldAllowUserToViewOwnTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(new BigDecimal("10.00"))
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldAllowAdminToViewAnyTransaction() {
        mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        User user = User.builder().id(2L).email("user@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(new BigDecimal("10.00"))
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrow_whenUserTriesToViewOthersTransaction() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        User other = User.builder().id(2L).email("other@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(other)
                .amount(new BigDecimal("10.00"))
                .transactionDate(LocalDate.now())
                .build();

        when(transactionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("You do not have permission to view this transaction");
    }

    @Test
    void findAll_shouldFilterByCurrentUserId_whenUserRole() {
        User user = mockAuthenticatedUser("user@example.com", Role.USER);
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = Transaction.builder().id(1L).user(user).amount(new BigDecimal("10.00")).transactionDate(LocalDate.now()).build();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAllWithFilters(user.getId(), null, null, null, null, null, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.findAll(null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userId()).isEqualTo(user.getId());
    }

    @Test
    void findAll_shouldAllowAdminToFilterByAnyUserId() {
        User admin = mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Pageable pageable = PageRequest.of(0, 10);
        User target = User.builder().id(2L).email("user@example.com").role(Role.USER).status(Status.ACTIVE).build();
        Transaction transaction = Transaction.builder().id(1L).user(target).amount(new BigDecimal("10.00")).transactionDate(LocalDate.now()).build();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAllWithFilters(2L, null, null, null, null, null, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.findAll(2L, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).userId()).isEqualTo(2L);
    }

    @Test
    void findAll_shouldAllowAdminToSeeAll_whenUserIdNull() {
        mockAuthenticatedUser("admin@example.com", Role.ADMIN);
        Pageable pageable = PageRequest.of(0, 10);
        Transaction transaction = Transaction.builder().id(1L).amount(new BigDecimal("10.00")).transactionDate(LocalDate.now()).build();
        Page<Transaction> page = new PageImpl<>(List.of(transaction));

        when(transactionRepository.findAllWithFilters(null, null, null, null, null, null, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.findAll(null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
