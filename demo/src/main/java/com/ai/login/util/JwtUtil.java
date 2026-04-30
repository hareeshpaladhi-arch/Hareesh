package com.ai.login.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    // 🔐 Generate Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // 🔍 Extract Username
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 🔍 Extract Expiration
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // 🔐 Validate Token
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isExpired(token);
    }

    // ⏳ Check Expiry
    private boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 📦 Get Claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}