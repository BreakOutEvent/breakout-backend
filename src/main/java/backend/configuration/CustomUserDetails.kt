package backend.configuration

import backend.model.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.Serializable

class CustomUserDetails : UserDetails, Serializable {

    companion object {
        private const val serialVersionUid: Long = 1L
    }

    val id: Long
    private val username: String
    private val isCredentialsNonExpired: Boolean
    private val isAccountNonExpired: Boolean
    private val isAccountNonLocked: Boolean
    private val authorities: Collection<GrantedAuthority>
    private val isEnabled: Boolean
    private val passwordHash: String

    constructor(user: User) {
        this.id = user.core.id!!
        this.username = user.email
        this.isCredentialsNonExpired = true
        this.isAccountNonExpired = true
        this.isAccountNonLocked = true
        this.authorities = user.core.getAuthorities().toMutableList()
        this.isEnabled = true
        this.passwordHash = user.passwordHash
    }


    override fun getUsername() = username

    override fun isCredentialsNonExpired() = isCredentialsNonExpired

    override fun isAccountNonExpired() = isAccountNonExpired

    override fun isAccountNonLocked() = isAccountNonLocked

    override fun getAuthorities() = authorities

    override fun isEnabled() = isEnabled

    override fun getPassword(): String? = passwordHash

    override fun equals(other: Any?): Boolean{
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

    override fun hashCode(): Int{
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

    override fun toString(): String{
        return "CustomUserDetails(id=$id, username='$username', isCredentialsNonExpired=$isCredentialsNonExpired, isAccountNonExpired=$isAccountNonExpired, isAccountNonLocked=$isAccountNonLocked, authorities=$authorities, isEnabled=$isEnabled, passwordHash='$passwordHash')"
    }
}
