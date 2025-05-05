package authentication.security.jwt

import org.springframework.beans.factory.annotation.Value
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component


@Component class JwtService(
    @Value("\${JWT_SECRET_KEY}") secretKeyBase64: String) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(secretKeyBase64)

    )
    private val expirationMs: Long = 1000 * 60 * 60 * 12 // 12 hours

    // Generate token with user ID, username, and role
    fun generateToken(id: Long?, username: String, role: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .setSubject(username)
            .claim("id", id.toString())  // Include user ID
            .claim("role", role)          // Include user role
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    // Extract claims from the token
    private fun extractClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

    // Extract the username (email) from token
    fun extractUsername(token: String): String = extractClaims(token).subject

    fun extractUserId(token: String): Long =
        (extractClaims(token)["id"] as Number).toLong()

    // Extract a user role from token
    fun extractRole(token: String): String = extractClaims(token)["role"] as String

    // Validate if the token is valid (checks subject and expiration)
    fun isTokenValid(token: String, username: String): Boolean {
        return try {
            extractUsername(token) == username && !isTokenExpired(token)
        } catch (e: Exception) {
            false
        }
    }

    // Check if the token has expired
    private fun isTokenExpired(token: String): Boolean =
        extractClaims(token).expiration.before(Date())
}

