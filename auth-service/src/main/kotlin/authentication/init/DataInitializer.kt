package authentication.init

import authentication.entity.UserEntity
import authentication.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.security.crypto.password.PasswordEncoder

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    // For testing only
    override fun run(vararg args: String?) {
        createUserIfNotExists("adminuser", "Password123", "ADMIN")
        createUserIfNotExists("testuser", "Password123", "USER")
    }

    private fun createUserIfNotExists(username: String, rawPassword: String, role: String) {
        if (userRepository.findByUsername(username) == null) {
            val user = UserEntity(
                username = username,
                password = passwordEncoder.encode(rawPassword),
                role = role
            )
            userRepository.save(user)
            println("Created user: $username")
        } else {
            println("User already exists: $username")
        }
    }
}