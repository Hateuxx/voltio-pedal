package com.voltiopedal.apiinventario.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration 
public class JwtConfig {
    @Bean SecretKey claveJwt(@Value("${app.jwt.secreto}") String secreto) {
        /* Validación de existencia */
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno JWT_SECRET. No se puede iniciar la seguridad.");
        }

        byte[] bytes;
        /* Validación de formato Base64 */
        try {
            bytes = Base64.getDecoder().decode(secreto);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("El valor de JWT_SECRET no es una cadena Base64 válida", e);
        }

        /* Validación de fuerza criptográfica (minimo 256 bits = 32 bytes) */
        if (bytes.length < 32) {
            throw new IllegalStateException("La clave JWT_SECRET es muy débil. Debe tener al menos 32 bytes.");
        }

        /* Retorno del sello */
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean 
    public JwtEncoder jwtEncoder(SecretKey claveJwt) {
        /* La maquina constructora de tokens */
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }
}
