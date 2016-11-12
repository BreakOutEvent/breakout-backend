package backend.model.user

import backend.model.BasicEntity
import backend.model.media.Media
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import javax.persistence.DiscriminatorColumn
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.ManyToOne
import kotlin.reflect.KClass

@Entity
@Inheritance
@DiscriminatorColumn(name = "ROLE_NAME")
abstract class UserRole : BasicEntity, User, GrantedAuthority {

    @ManyToOne
    @JsonIgnore
    override lateinit var core: UserCore

    /**
     * No args constructor needed for
     * private no args constructor of subclasses used for JPA
     */
    constructor() : super()

    constructor(core: UserCore) : this() {
        this.core = core
    }

    // From here on: delegation of methods to core

    override var firstname: String?
        get() = this.core.firstname
        set(firstname) {
            this.core.firstname = firstname
        }

    override var lastname: String?
        get() = this.core.lastname
        set(lastname) {
            this.core.lastname = lastname
        }

    override var email: String
        get() = this.core.email
        set(email) {
            this.core.email = email
        }

    override var isBlocked: Boolean
        get() = this.core.isBlocked
        set(isBlocked) {
            this.core.isBlocked = isBlocked
        }

    override var passwordHash: String
        get() = this.core.passwordHash
        set(password) {
            this.core.passwordHash = password
        }

    override var gender: String?
        get() = this.core.gender
        set(gender) {
            this.core.gender = gender
        }

    override var profilePic: Media
        get() = this.core.profilePic
        set(profilePic) {
            this.core.profilePic = profilePic
        }

    override fun <T : UserRole> addRole(clazz: KClass<T>): T = this.core.addRole(clazz)
    override fun <T : UserRole> getRole(clazz: KClass<T>): T? = this.core.getRole(clazz)
    override fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean = this.core.hasRole(clazz)
    override fun <T : UserRole> removeRole(clazz: KClass<T>): T? = this.core.removeRole(clazz)

    override fun activate(token: String) = this.core.activate(token)
    override fun isActivationTokenCorrect(token: String): Boolean = this.core.isActivationTokenCorrect(token)
    override fun createActivationToken(): String = this.core.createActivationToken()
    override fun isActivated(): Boolean = this.core.isActivated()
    override fun setNewPassword(password: String, token: String) = this.core.setNewPassword(password, token)

    companion object {

        /**
         * Create new instance of a subclass of userRole
         * @param clazz The class of which an instance should be created. Must be subclass of UserRole
         *              and have a noargs constructor
         * @param core  The core with the UserRole will have
         * @return Returns a new instance of the specified class with a corresponding UserCore
         */
        @Suppress("UNCHECKED_CAST")
        @Throws(Exception::class)
        fun <T : UserRole> createFor(clazz: Class<T>, core: UserCore): UserRole {
            val constructor = clazz.declaredConstructors.filter { it.parameterCount == 0 }.firstOrNull()
                    ?: throw Exception("no args constructor not found on $clazz")
            constructor.isAccessible = true
            val o = constructor.newInstance() as T
            o.core = core
            return o
        }
    }
}
