package backend.configuration

import backend.model.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService @Autowired constructor(private val userService: UserService) : UserDetailsService {

    private val logger: Logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    @Transactional
    override fun loadUserByUsername(username: String): UserDetails? {
        userService.getUserByEmail(username)?.let { user ->
            return CustomUserDetails(user)
        }

        logger.warn("User $username does not exist")
        throw UsernameNotFoundException("User $username does not exist")
    }

}
