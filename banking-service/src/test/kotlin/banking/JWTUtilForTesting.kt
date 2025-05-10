package banking


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import javax.crypto.SecretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class JwtUtilForTesting (
    @Value("\${JWT_SECRET_KEY}")
    secretKeyBase64: String
) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(
        Base64.getDecoder().decode(secretKeyBase64))

    // Extract claims from the token
    private fun extractClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

    fun extractUserId(token: String): Long {
        val idClaim = extractClaims(token)["id"]
        return when (idClaim) {
            is Number -> idClaim.toLong()
            is String -> idClaim.toLong()
            else -> throw IllegalArgumentException("Invalid ID type in token")
        }
    }
    fun extractRole(token: String): String = extractClaims(token)["role"] as String




}