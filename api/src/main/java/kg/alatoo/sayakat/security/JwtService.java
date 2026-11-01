package kg.alatoo.sayakat.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kg.alatoo.sayakat.user.Role;
import kg.alatoo.sayakat.user.User;

@Service
public class JwtService {

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        if (!StringUtils.hasText(properties.secret())) {
            throw new IllegalStateException("sayakat.jwt.secret (JWT_SECRET) must be set");
        }
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String issue(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.ttl())))
                .signWith(key)
                .compact();
    }

    public AuthUser parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthUser(Long.valueOf(claims.getSubject()), claims.get("email", String.class),
                Role.valueOf(claims.get("role", String.class)));
    }

    public long ttlSeconds() {
        return properties.ttl().toSeconds();
    }
}
