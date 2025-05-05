package authentication.security

import authentication.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
        if (user != null) {
            return UserPrincipal(
                id = user.id,
                username = user.username,
                role = user.role,
            )
        }

        val admin = userRepository.findByUsername(username)
        if (admin != null) {
            return UserPrincipal(
                id = admin.id,
                username = admin.username,
                role = admin.role)
        }

        throw UsernameNotFoundException("User not found with username: $username")
    }


}