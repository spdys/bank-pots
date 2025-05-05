package authentication.security.jwt

import authentication.security.CustomUserDetailsService
import authentication.security.UserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)
        val username = jwtService.extractUsername(token)

        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            if (jwtService.isTokenValid(token, username)) {
                val userDetails = userDetailsService.loadUserByUsername(username)

                val userPrincipal = if (userDetails is UserPrincipal) {
                    UserPrincipal(
                        id = userDetails.getUserId(),
                        username = userDetails.username,
                        role = userDetails.authorities.first().authority.removePrefix("ROLE_"),
                        token = token,
                    )
                } else {
                    userDetails
                }

                val authToken = UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userDetails.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}