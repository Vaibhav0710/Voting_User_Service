package com.voting.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Centralized JWT operations — token generation, parsing, and validation.
 * <p>
 * Design decisions:
 * <ul>
 *   <li><b>HMAC-SHA256 (HS256):</b> Symmetric signing — same key for sign and verify.
 *       Appropriate for a single auth service. If multiple services need to verify
 *       independently, switch to RS256 (asymmetric) with a public/private keypair.</li>
 *   <li><b>Claims include userId + role:</b> Downstream services can extract user context
 *       without a database lookup on every request.</li>
 *   <li><b>Secret key loaded from environment:</b> Never hardcoded. Must be Base64-encoded,
 *       minimum 256 bits.</li>
 * </ul>
 * </p>
 *
 * @see <a href="https://github.com/jwtk/jjwt">JJWT GitHub</a>
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    /**
     * Constructs JwtService with externalized configuration.
     * <p>
     * The secret is Base64-decoded once at startup and cached as a {@link SecretKey}
     * to avoid repeated decoding on every token operation.
     * </p>
     *
     * @param secret      Base64-encoded secret key (from {@code jwt.secret} property)
     * @param expirationMs token lifetime in milliseconds (from {@code jwt.expiration} property)
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        log.info("JwtService initialized — expiration={}ms, algorithm=HS256", expirationMs);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TOKEN GENERATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Generates a JWT token for an authenticated user.
     * <p>
     * Token payload structure:
     * <pre>
     * {
     *   "sub": "vaibhav_jain",          ← username (subject)
     *   "userId": "uuid-string",       ← for downstream services
     *   "role": "ROLE_VOTER",          ← for @PreAuthorize checks
     *   "iat": 1714000000,             ← issued-at timestamp
     *   "exp": 1714086400              ← expiration timestamp
     * }
     * </pre>
     * </p>
     *
     * @param username the username to set as the token subject
     * @param userId   the user's UUID (included as a custom claim)
     * @param role     the user's role (ROLE_VOTER or ROLE_ADMIN)
     * @return signed JWT token string
     */
    public String generateToken(String username, UUID userId, String role) {
        return generateToken(username, Map.of(
                "userId", userId.toString(),
                "role", role
        ));
    }

    /**
     * Generates a JWT with arbitrary extra claims.
     * <p>
     * Kept as an overload for flexibility — other services or future features
     * may need additional claims (e.g., electionId scope, permissions list).
     * </p>
     *
     * @param username    the subject
     * @param extraClaims additional claims to embed in the payload
     * @return signed JWT token string
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claims(extraClaims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TOKEN PARSING (CLAIM EXTRACTION)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Extracts the username (subject) from a token.
     *
     * @param token the JWT string
     * @return the username embedded as the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the userId claim from a token.
     *
     * @param token the JWT string
     * @return the user's UUID
     */
    public UUID extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userId);
    }

    /**
     * Extracts the role claim from a token.
     *
     * @param token the JWT string
     * @return the role string (e.g., "ROLE_VOTER")
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts the expiration date from a token.
     *
     * @param token the JWT string
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor using a function reference.
     * <p>
     * This pattern avoids duplicating the parsing logic across multiple
     * extract methods. Example usage:
     * <pre>
     *   extractClaim(token, Claims::getSubject)
     *   extractClaim(token, c -> c.get("userId", String.class))
     * </pre>
     * </p>
     *
     * @param token          the JWT string
     * @param claimsResolver function that extracts the desired claim
     * @param <T>            the claim's return type
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TOKEN VALIDATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Validates a token against a given username.
     * <p>
     * Two-layer validation:
     * <ol>
     *   <li><b>Subject match:</b> The token's subject must equal the expected username.
     *       This prevents token reuse if a user's username changes.</li>
     *   <li><b>Expiration check:</b> Rejects expired tokens. JJWT throws
     *       {@link ExpiredJwtException} automatically during parsing, but we
     *       also check explicitly for clarity and defense-in-depth.</li>
     * </ol>
     * </p>
     *
     * @param token    the JWT string
     * @param username the expected username to match against
     * @return {@code true} if the token is valid for this user
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expired for user: {}", username);
            return false;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a token has expired.
     *
     * @param token the JWT string
     * @return {@code true} if the token's expiration is before the current time
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Parses and verifies the token signature, returning all claims.
     * <p>
     * This is the single point where JJWT performs:
     * <ol>
     *   <li>Base64 decoding of header, payload, signature</li>
     *   <li>HMAC-SHA256 signature verification against our signing key</li>
     *   <li>Expiration / not-before validation</li>
     * </ol>
     * If any step fails, JJWT throws a specific exception (ExpiredJwtException,
     * SignatureException, MalformedJwtException, etc.).
     * </p>
     *
     * @param token the JWT string
     * @return all claims from the token payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
