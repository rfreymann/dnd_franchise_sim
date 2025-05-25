package dnd.franchise.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT provider using JJWT library with constructor-based configuration.
 * Ensures the Base64-encoded secret is at least 256 bits (32 bytes).
 */
@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final JwtParser jwtParser;

    /**
     * Decodes the Base64-encoded secret and initializes the JwtParser.
     * Validates that the secret is at least 256 bits for HS256.
     *
     * @param base64Key the Base64-encoded HMAC secret (must decode to >= 32 bytes)
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "Weak JWT secret: decoded key is " + (keyBytes.length * 8) +
                " bits. HS256 requires at least 256 bits (32 bytes). " +
                "Generate a 256-bit secret, e.g. `openssl rand -base64 32`.");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(this.secretKey)
                .build();
    }

    /**
     * Creates an HS256 JWT with the given subject and validity (hours).
     *
     * @param subject    the token subject (e.g., username)
     * @param validHours validity duration in hours
     * @return compact JWT string
     */
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

    /**
     * Parses and validates the given JWT.
     *
     * @param token the JWT string
     * @return parsed JWS claims
     */
    public Jws<Claims> parseToken(String token) {
        return jwtParser.parseClaimsJws(token);
    }
}
