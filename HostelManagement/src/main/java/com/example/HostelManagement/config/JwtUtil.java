package com.example.HostelManagement.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
//class is responsible  called during a successful login to create a new, signed JWT for the user.
public class JwtUtil {
    // Security: Initializes a private, final cryptographic key used to sign and verify all JWTs. 
    // HS256 specifies the HMAC using SHA-256 algorithm. The key is kept secret on the server.
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    //  Defines the token's lifespan in milliseconds (24 hours in this case).
    //  After this time, the token is considered invalid.Defines the token's lifespan in milliseconds (24 hours in this case).
    //  After this time, the token is considered invalid.

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    //This method is called during a successful login to create a new, signed JWT for the user.
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    //These methods are used by the JwtAuthFilter to verify the token sent by the browser on subsequent requests.
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}