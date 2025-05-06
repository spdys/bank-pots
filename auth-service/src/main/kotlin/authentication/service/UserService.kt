package authentication.service

import authentication.dto.UserCreationRequest
import authentication.entity.UserEntity
import authentication.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun validatePassword(password: String) {
        require(password.length >= 8) { "Password must be at least 8 characters long" }
        require(password.any { it.isUpperCase() }) { "Password must contain at least one uppercase letter" }
        require(password.any { it.isDigit() }) { "Password must contain at least one number" }
    }

    fun createUser(user: UserCreationRequest): ResponseEntity<String?> {

        validatePassword(user.password)

        userRepository.save(UserEntity(username = user.username, password = passwordEncoder.encode(user.password)))
        return ResponseEntity.ok().body("User registered successfully")
    }
}