package authentication.service

import authentication.dto.UserCreationRequest
import authentication.entity.UserEntity
import authentication.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun createUser(user: UserCreationRequest) {
        userRepository.save(UserEntity(username = user.username, password = passwordEncoder.encode(user.password)))
    }
}