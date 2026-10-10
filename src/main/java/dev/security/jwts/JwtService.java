package dev.security.jwts;

import dev.users.security.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long jwtExpirationMinute;


    public JwtService(
            @Value("${application.security.jwt.secret-key}") String secretKeyRaw,
            @Value("${application.security.jwt.jwt-expiration-minute}") long jwtExpirationMinute) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKeyRaw);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationMinute = jwtExpirationMinute;
    }

    //Токен з додатковими полями tenantId, userRole
    public String generateToken(Long tenantId, UserRole userRole, UserDetails userDetails){
        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim("tenantId", tenantId)
                .claim("userRole", userRole)
                .claim("userAuthorities", userRole.getAuthorities())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + jwtExpirationMinute * 60 * 1000))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokeValid(String token, UserDetails userDetails){
        try {
            String username = extractUsername(token);
            return userDetails.getUsername().equals(username);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token structure: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT token is missing or empty: {}", e.getMessage());
        }
        return false;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
