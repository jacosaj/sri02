package edu.pja.sri.s34669.sri02.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtIssuer {

    private final JwtProperties jwtProperties;

    public String issue(UserPrincipal userPrincipal) {
        String secretKey = jwtProperties.getSecretKey();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withSubject(String.valueOf(userPrincipal.getUserId()))
                .withExpiresAt(Instant.now().plus(Duration.ofMinutes(60)))
                .withClaim("email", userPrincipal.getEmail())
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC256(secretKey));
    }
}
