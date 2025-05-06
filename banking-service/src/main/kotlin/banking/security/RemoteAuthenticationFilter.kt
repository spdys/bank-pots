package banking.security

import banking.client.AuthenticationClient
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RemoteAuthenticationFilter(
    private val authenticationClient: AuthenticationClient,
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
        val result = authenticationClient.checkToken(token)

        val userPrincipal = UserPrincipal(
            id = result.userId,
            username = result.username,
            role = result.role,
            token = token,
            password = ""
        )

        val auth = UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_${result.role}"))
        )

        SecurityContextHolder.getContext().authentication = auth
        filterChain.doFilter(request, response)
    }
}