package com.fcs.mis_fichas.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_shouldHaveViolations_whenEmailInvalid() {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123", "User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("email");
    }

    @Test
    void registerRequest_shouldHaveViolations_whenPasswordTooShort() {
        RegisterRequest request = new RegisterRequest("user@example.com", "12345", "User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("password");
    }

    @Test
    void registerRequest_shouldHaveViolations_whenNameBlank() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "   ");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("name");
    }

    @Test
    void registerRequest_shouldHaveNoViolations_whenValid() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123", "User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_shouldHaveViolations_whenEmailBlank() {
        LoginRequest request = new LoginRequest("", "password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("email");
    }

    @Test
    void loginRequest_shouldHaveNoViolations_whenValid() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void categoryRequest_shouldHaveViolations_whenNameBlank() {
        CategoryRequest request = new CategoryRequest("", null);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("name", "type");
    }

    @Test
    void categoryRequest_shouldHaveNoViolations_whenValid() {
        CategoryRequest request = new CategoryRequest("Food", com.fcs.mis_fichas.enums.Type.EXPENSE);

        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void subcategoryRequest_shouldHaveViolations_whenNameBlank() {
        SubcategoryRequest request = new SubcategoryRequest("", 1L, null);

        Set<ConstraintViolation<SubcategoryRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("name");
    }

    @Test
    void subcategoryRequest_shouldHaveNoViolations_whenValid() {
        SubcategoryRequest request = new SubcategoryRequest("Groceries", 1L, "Comments");

        Set<ConstraintViolation<SubcategoryRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void transactionRequest_shouldHaveViolations_whenAmountNotPositive() {
        TransactionRequest request = new TransactionRequest(1L, 1L, 1L, new BigDecimal("0.00"), "Desc", LocalDate.now());

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("amount");
    }

    @Test
    void transactionRequest_shouldHaveViolations_whenDateNull() {
        TransactionRequest request = new TransactionRequest(1L, 1L, 1L, new BigDecimal("10.00"), "Desc", null);

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("transactionDate");
    }

    @Test
    void transactionRequest_shouldHaveNoViolations_whenValid() {
        TransactionRequest request = new TransactionRequest(1L, 1L, 1L, new BigDecimal("10.00"), "Desc", LocalDate.now());

        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void userUpdateRequest_shouldHaveViolations_whenEmailInvalid() {
        UserUpdateRequest request = new UserUpdateRequest("Name", "invalid", com.fcs.mis_fichas.enums.Role.USER, com.fcs.mis_fichas.enums.Status.ACTIVE);

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).hasSizeGreaterThanOrEqualTo(1);
        assertThat(violations.stream().map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet()))
                .contains("email");
    }

    @Test
    void userUpdateRequest_shouldHaveNoViolations_whenValid() {
        UserUpdateRequest request = new UserUpdateRequest("Name", "user@example.com", com.fcs.mis_fichas.enums.Role.USER, com.fcs.mis_fichas.enums.Status.ACTIVE);

        Set<ConstraintViolation<UserUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
