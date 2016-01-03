package backend

import backend.model.user.User
import backend.model.user.UserCore
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

private class CustomUserDetails(val user: User) : UserCore(), UserDetails {
    override fun getUsername() = user.email

    override fun isCredentialsNonExpired() = true

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getAuthorities() = user.core!!.userRoles.values

    override fun isEnabled() = !user.isBlocked

    override fun getPassword(): String? = user.passwordHash
}