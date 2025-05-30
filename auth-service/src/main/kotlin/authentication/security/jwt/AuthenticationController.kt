package authentication.security.jwt

import authentication.security.CustomUserDetailsService
import authentication.security.UserPrincipal
import authentication.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
@RequestMapping("api/v1/users/auth")
class AuthenticationController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val jwtService: JwtService,
    private val userService: UserService
) {
    @PostMapping("/login")

    fun login(@RequestBody authRequest: AuthenticationRequest): AuthenticationResponse {
        val authToken = UsernamePasswordAuthenticationToken(authRequest.username, authRequest.password)
        val authentication = authenticationManager.authenticate(authToken)

        if (authentication.isAuthenticated) {
            val userDetails = userDetailsService.loadUserByUsername(authRequest.username)

            // Ensure userDetails is of type UserPrincipal
            if (userDetails is UserPrincipal) {
                val id = userDetails.getId()  // Get the user's ID from UserPrincipal
                val email = userDetails.username  // Get the user's email from UserPrincipal
                val role =
                    userDetails.authorities.first().authority.removePrefix("ROLE_")  // Extract role without "ROLE_" prefix

                // Generate JWT token with user ID, email, and role
                val token = jwtService.generateToken(id, email, role)

                return AuthenticationResponse(token)
            } else {
                throw UsernameNotFoundException("Invalid user request!")
            }
        } else {
            throw UsernameNotFoundException("Invalid user request!")
        }
    }
    @PostMapping("/check-token")
    fun checkToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<CheckTokenResponse> {
        val token = authHeader.removePrefix("Bearer ").trim()
        val username = jwtService.extractUsername(token)
        val role = jwtService.extractRole(token)
        val userId = jwtService.extractUserId(token)
        return ResponseEntity.ok(CheckTokenResponse(userId, username, role))
    }
}

data class AuthenticationRequest(
    val username: String,
    val password: String
)

data class AuthenticationResponse(
    val token: String
)

data class CheckTokenResponse(
    val userId: Long,
    val username: String,
    val role: String
)