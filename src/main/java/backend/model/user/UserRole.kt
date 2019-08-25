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
    override lateinit var account: UserAccount

    /**
     * No args constructor needed for
     * private no args constructor of subclasses used for JPA
     */
    constructor() : super()

    constructor(account: UserAccount) : this() {
        this.account = account
    }

    // From here on: delegation of methods to account

    override var firstname: String?
        get() = this.account.firstname
        set(firstname) {
            this.account.firstname = firstname
        }

    override var lastname: String?
        get() = this.account.lastname
        set(lastname) {
            this.account.lastname = lastname
        }

    override var email: String
        get() = this.account.email
        set(email) {
            this.account.email = email
        }

    override var isBlocked: Boolean
        get() = this.account.isBlocked
        set(isBlocked) {
            this.account.isBlocked = isBlocked
        }

    override var passwordHash: String
        get() = this.account.passwordHash
        set(password) {
            this.account.passwordHash = password
        }

    override var gender: String?
        get() = this.account.gender
        set(gender) {
            this.account.gender = gender
        }

    override var profilePic: Media?
        get() = this.account.profilePic
        set(profilePic) {
            this.account.profilePic = profilePic
        }

    override var notificationToken: String?
        get() = this.account.notificationToken
        set(notificationToken) {
            this.account.notificationToken = notificationToken
        }

    override var preferredLanguage: Language
        get() = this.account.preferredLanguage
        set(value) {
            this.account.preferredLanguage = value
        }


    override fun emailDomain(): String {
        return email.split("@").last()
    }

    override fun <T : UserRole> addRole(clazz: KClass<T>): T = this.account.addRole(clazz)
    override fun <T : UserRole> getRole(clazz: KClass<T>): T? = this.account.getRole(clazz)
    override fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean = this.account.hasRole(clazz)
    override fun <T : UserRole> removeRole(clazz: KClass<T>): T? = this.account.removeRole(clazz)

    override fun activate(token: String) = this.account.activate(token)
    override fun isActivationTokenCorrect(token: String): Boolean = this.account.isActivationTokenCorrect(token)
    override fun createActivationToken(): String = this.account.createActivationToken()
    override fun isActivated(): Boolean = this.account.isActivated()
    override fun setNewPassword(password: String, token: String) = this.account.setNewPassword(password, token)

    companion object {

        /**
         * Create new instance of a subclass of userRole
         * @param clazz The class of which an instance should be created. Must be subclass of UserRole
         *              and have a noargs constructor
         * @param account  The account with the UserRole will have
         * @return Returns a new instance of the specified class with a corresponding UserAccount
         */
        @Suppress("UNCHECKED_CAST")
        @Throws(Exception::class)
        fun <T : UserRole> createFor(clazz: Class<T>, account: UserAccount): UserRole {
            val constructor = clazz.declaredConstructors.firstOrNull { it.parameterCount == 0 }
                    ?: throw Exception("no args constructor not found on $clazz")
            constructor.isAccessible = true
            val o = constructor.newInstance() as T
            o.account = account
            return o
        }
    }
}
