package backend.configuration

import backend.model.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService : UserDetailsService {

    val userService: UserService

    @Autowired
    constructor(userService: UserService) {
        this.userService = userService
    }

    override fun loadUserByUsername(username: String): UserDetails? {
        userService.getUserByEmail(username)?.let { user ->
            return CustomUserDetails(user)
        }

        throw UsernameNotFoundException("User $username does not exist")
    }

}
