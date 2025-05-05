package authentication.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    private val id: Long?,
    private val username: String,
    private val role: String,
    private val token: String? = null,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_$role"))
    }

    override fun getPassword(): String = password
    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    fun getId(): Long? = id
    fun getJwtToken(): String? = token

    fun getUserId(): Long? = if (role.equals("USER", ignoreCase = true)) id else null
    fun getAdminId(): Long? = if (role.equals("ADMIN", ignoreCase = true)) id else null
}