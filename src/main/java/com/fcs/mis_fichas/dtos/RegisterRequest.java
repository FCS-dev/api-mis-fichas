package com.fcs.mis_fichas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de solicitud para registrar un nuevo usuario.
 *
 * @param email    correo electronico del usuario (obligatorio, formato valido)
 * @param password contrasena del usuario (obligatorio, minimo 6 caracteres)
 * @param name     nombre completo del usuario (obligatorio)
 */
public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6) String password,
    @NotBlank String name
) {
}
