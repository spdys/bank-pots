package authentication.controller

import authentication.entity.UserEntity
import authentication.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(val userRepository: UserRepository) {

    @PostMapping("/register")
    fun registerUser(@RequestBody user: UserDto): ResponseEntity<Any> {

        return try {
            ResponseEntity.ok()
                .body(userRepository.save(UserEntity(username = user.username, password = user.password)))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }

    }
}

data class UserDto(val username: String, val password: String)