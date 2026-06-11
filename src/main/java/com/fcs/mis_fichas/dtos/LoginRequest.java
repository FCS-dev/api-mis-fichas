package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de solicitud para iniciar sesion.
 *
 * @param email    correo electronico del usuario (obligatorio, formato valido)
 * @param password contrasena del usuario (obligatorio)
 */
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {
}
