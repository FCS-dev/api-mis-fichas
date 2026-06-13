package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para iniciar sesión.
 *
 * @param email    correo electrónico del usuario (obligatorio, formato válido)
 * @param password contraseña del usuario (obligatorio)
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
