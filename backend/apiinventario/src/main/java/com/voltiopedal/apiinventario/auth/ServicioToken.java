package com.voltiopedal.apiinventario.auth;

import com.voltiopedal.apiinventario.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service 
public class ServicioToken {
    private final JwtEncoder jwtEncoder;
    private final Duration expiracion;

    public ServicioToken(JwtEncoder jwtEncoder, @Value("${app.jwt.expiracion}") Duration expiracion) {
        this.jwtEncoder = jwtEncoder;
        this.expiracion = expiracion;
    }

    /* El record resuelve la necesidad de devolver 2 datos a la vez */
    public record TokenGenerado(String token, Instant vence) {}

    public TokenGenerado crearToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant vence = ahora.plus(expiracion);

        JwtClaimsSet contenido = JwtClaimsSet.builder()
                .issuer("voltio-pedal")
                .issuedAt(ahora)
                .expiresAt(vence)
                .subject(String.valueOf(usuario.getIdUsuario())) /* Identificador inmutable */
                .claim("rol", usuario.getRol().name()) /* Se usa String por portabilidad */
                .build();
        
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(cabecera, contenido)).getTokenValue();

        return new TokenGenerado(token, vence);
    }
}
