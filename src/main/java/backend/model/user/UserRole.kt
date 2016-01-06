package backend.model.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority

import javax.persistence.*

@Entity
@Inheritance
@DiscriminatorColumn(name = "ROLE_NAME")
abstract class UserRole() : User, GrantedAuthority {

    @Id
    @GeneratedValue
    var roleID: Int? = null

    @ManyToOne
    @JsonIgnore
    override var core: UserCore? = null

    constructor(core: UserCore) : this() {
        this.core = core
    }

    override var firstname: String?
        get() = this.core!!.firstname
        set(firstname) {
            this.core!!.firstname = firstname
        }

    override var lastname: String?
        get() = this.core!!.lastname
        set(lastname) {
            this.core!!.lastname = lastname
        }

    override var email: String
        get() = this.core!!.email
        set(email) {
            this.core!!.email = email
        }

    override var isBlocked: Boolean
        get() = this.core!!.isBlocked
        set(isBlocked) {
            this.core!!.isBlocked = true
        }

    override var passwordHash: String
        get() = this.core!!.passwordHash
        set(password) {
            this.core!!.passwordHash = password
        }

    override var gender: String?
        get() = this.core!!.gender
        set(gender) {
            this.core!!.gender = gender
        }

    override fun addRole(clazz: Class<out UserRole>): UserRole = this.core!!.addRole(clazz)
    override fun getRole(clazz: Class<out UserRole>): UserRole? = this.core!!.getRole(clazz)
    override fun hasRole(clazz: Class<out UserRole>): Boolean = this.core!!.hasRole(clazz)
    override fun removeRole(clazz: Class<out UserRole>): UserRole? = this.core!!.removeRole(clazz)

    companion object {

        @Throws(Exception::class)
        fun createFor(clazz: Class<out UserRole>, core: UserCore): UserRole {
            val o = clazz.newInstance();
            o.core = core;
            return o;
        }
    }
}
