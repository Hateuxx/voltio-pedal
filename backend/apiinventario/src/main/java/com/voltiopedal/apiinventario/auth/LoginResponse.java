package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.Rol;
import java.time.Instant;

public record LoginResponse(
    String token,
    String tipoToken,
    Instant fechaExpiracion,
    String nombres,
    String apellidos,
    Rol rol
) {
    public LoginResponse(String token, Instant fechaExpiracion, String nombres, String apellidos, Rol rol) {
        this(token, "Bearer", fechaExpiracion, nombres, apellidos, rol);
    }
}
