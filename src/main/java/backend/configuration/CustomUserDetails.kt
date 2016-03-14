package backend.configuration

import backend.model.user.User
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(val user: User) : UserDetails {

    val id = user.core.id

    override fun getUsername() = user.email

    override fun isCredentialsNonExpired() = true

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun getAuthorities() = user.core.getAuthorities()

    override fun isEnabled() = !user.isBlocked

    override fun getPassword(): String? = user.passwordHash
}
