package authentication.controller

import authentication.dto.UserCreationRequest
import authentication.entity.UserEntity
import authentication.repository.UserRepository
import authentication.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/users")
class UserController(val userRepository: UserRepository, private val userService: UserService) {

    @PostMapping("/register")
    fun registerUser(@RequestBody user: UserCreationRequest){
        userService.createUser(user)
    }




}
