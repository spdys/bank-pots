package authentication.security.jwt

import authentication.security.CustomUserDetailsService
import authentication.security.UserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.text.startsWith
import kotlin.text.substring


@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("JwtAuthenticationFilter triggered")
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        val username = jwtService.extractUsername(token)
        println("Extracted username: $username")
        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            println("Username extracted and no authentication in context")
            if (jwtService.isTokenValid(token, username)) {
                println("Token is valid for username: $username")
                val userDetails = userDetailsService.loadUserByUsername(username)
                val roleFromJwt = jwtService.extractRole(token) // Extract directly from token
                println("Role extracted from JWT: $roleFromJwt")
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$roleFromJwt")) // Add correct role prefix
                println("Created authorities: $authorities")


                val userPrincipal = if (userDetails is UserPrincipal) {
                   val principal = UserPrincipal(
                        id = userDetails.getUserId(),
                        username = userDetails.username,
                        role = userDetails.authorities.first().authority.removePrefix("ROLE_"),
                        token = token,
                        password = userDetails.password,
                    )
                    println("Created UserPrincipal with role: ${principal.authorities}")
                    principal
                } else {
                    println("Using original userDetails")
                    userDetails
                }

                val authToken = UsernamePasswordAuthenticationToken(userPrincipal, null, authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
                println("Set authentication in SecurityContext with authorities: ${authToken.authorities}")

            } else
                println("Token is NOT valid for username: $username")

        }

        filterChain.doFilter(request, response)
    }
}