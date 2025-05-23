package dnd.franchise.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Configuration for JWT handling: provides TokenProvider and JwtDecoder beans.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String base64Key;

    /**
     * JJWT-based token provider for creating and parsing JWTs.
     */
    @Bean
    public JwtTokenProvider tokenProvider() {
        return new JwtTokenProvider(base64Key);
    }

    /**
     * NimbusJwtDecoder bean so Spring Security can validate incoming tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Internal provider class using JJWT for token operations.
     */
    public static class JwtTokenProvider {

        private final Key secretKey;
        private final JwtParser jwtParser;

        public JwtTokenProvider(String base64Key) {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException(
                    "Weak JWT secret: must decode to >= 32 bytes for HS256");
            }
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            this.jwtParser = Jwts.parserBuilder()
                    .setSigningKey(this.secretKey)
                    .build();
        }

        public String createToken(String subject, long validHours) {
            Instant now = Instant.now();
            Instant expiry = now.plus(validHours, ChronoUnit.HOURS);
            return Jwts.builder()
                    .setSubject(subject)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        }

        public Jws<Claims> parseToken(String token) {
            return jwtParser.parseClaimsJws(token);
        }
    }
}
