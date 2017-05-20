package backend.configuration

import backend.model.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

class CustomUserDetails(user: User) : UserDetails, Serializable {

    companion object {
        private const val serialVersionUid: Long = 1L
    }

    val id: Long = user.account.id!!
    private val username: String = user.email
    private val isCredentialsNonExpired: Boolean = true
    private val isAccountNonExpired: Boolean = true
    private val isAccountNonLocked: Boolean = true
    private val authorities: Collection<GrantedAuthority> = user.account.getAuthorities().toMutableList()
    private val isEnabled: Boolean = true
    private val passwordHash: String = user.passwordHash


    override fun getUsername() = username

    override fun isCredentialsNonExpired() = isCredentialsNonExpired

    override fun isAccountNonExpired() = isAccountNonExpired

    override fun isAccountNonLocked() = isAccountNonLocked

    override fun getAuthorities() = authorities

    override fun isEnabled() = isEnabled

    override fun getPassword(): String? = passwordHash

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomUserDetails) return false

        if (id != other.id) return false
        if (username != other.username) return false
        if (isCredentialsNonExpired != other.isCredentialsNonExpired) return false
        if (isAccountNonExpired != other.isAccountNonExpired) return false
        if (isAccountNonLocked != other.isAccountNonLocked) return false
        if (authorities != other.authorities) return false
        if (isEnabled != other.isEnabled) return false
        if (passwordHash != other.passwordHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result += 31 * result + username.hashCode()
        result += 31 * result + isCredentialsNonExpired.hashCode()
        result += 31 * result + isAccountNonExpired.hashCode()
        result += 31 * result + isAccountNonLocked.hashCode()
        result += 31 * result + authorities.hashCode()
        result += 31 * result + isEnabled.hashCode()
        result += 31 * result + passwordHash.hashCode()
        return result
    }

    override fun toString(): String {
        return "CustomUserDetails(id=$id, username='$username', isCredentialsNonExpired=$isCredentialsNonExpired, isAccountNonExpired=$isAccountNonExpired, isAccountNonLocked=$isAccountNonLocked, authorities=$authorities, isEnabled=$isEnabled, passwordHash='$passwordHash')"
    }
}
