package backend.model.user

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.media.Media
import backend.model.messaging.GroupMessage
import backend.model.payment.Payment
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

@Entity
class UserAccount : BasicEntity, User {

    constructor() : super() {
        this.isBlocked = true
        this.profilePic = null
        this.preferredLanguage = Language.DE
    }

    @Email
    @Column(unique = true, nullable = false)
    override lateinit var email: String

    @NotEmpty
    override lateinit var passwordHash: String

    override var firstname: String? = null

    override var lastname: String? = null

    override var isBlocked = false

    override var gender: String? = null

    override var notificationToken: String? = null

    override var newsletter: Boolean = false

    @Enumerated(EnumType.STRING)
    override var preferredLanguage: Language = Language.DE

    @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true)
    override var profilePic: Media? = null

    @ManyToMany(mappedBy = "users", cascade = [(CascadeType.ALL)])
    val groupMessages: MutableList<GroupMessage> = ArrayList()

    @OneToMany(mappedBy = "user", cascade = [(CascadeType.ALL)])
    val payments: MutableList<Payment> = ArrayList()

    @ManyToMany
    var blockedBy: MutableList<UserAccount> = ArrayList()

    /*
     * cascade all operations to children
     * orphanRemoval = true allows removing a role from the database
     * if it gets removed from Map userRoles and the account is saved!
     * See: http://stackoverflow.com/a/2011546
     */
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    private var userRoles: MutableMap<Class<out UserRole>, UserRole> = HashMap()

    private var activationToken: String? = null

    private fun getAllRoles(): Collection<UserRole> {
        return this.userRoles.values
                .flatMap { it.getAuthorities() }
    }

    fun getAuthorities(): Collection<GrantedAuthority> {
        return getAllRoles().map(::BasicGrantedAuthority)
    }

    override fun activate(token: String) {
        if (isActivationTokenCorrect(token)) {
            this.isBlocked = false
            this.activationToken = null
        } else {
            throw DomainException("Provided token $token does not match the activation token")
        }
    }

    override fun isActivationTokenCorrect(token: String): Boolean {
        return this.activationToken == token
    }

    override fun createActivationToken(): String {
        this.activationToken = UUID.randomUUID().toString()
        return this.activationToken!!
    }

    override fun isActivated(): Boolean {
        return !isBlocked
    }

    override fun setNewPassword(password: String, token: String) {

        //successful password reset is also email validation
        this.activate(token)
        this.passwordHash = BCryptPasswordEncoder().encode(password)
    }

    // This cast will always succeed because the specific type of the value / object
    // in the Map<Class<out UserRole>, UserRole> always matches the one defined with T
    // This is guaranteed as the key of the map always expresses the exact class of
    // the object stored as the value
    @Suppress("UNCHECKED_CAST")
    override fun <T : UserRole> getRole(clazz: KClass<T>): T? = userRoles[clazz.java] as? T?

    override fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean = userRoles.containsKey(clazz.java)

    @Suppress("UNCHECKED_CAST")
    override fun <T : UserRole> removeRole(clazz: KClass<T>): T? = userRoles.remove(clazz.java) as? T?

    @Throws(Exception::class)
    @Suppress("UNCHECKED_CAST")
    override fun <T : UserRole> addRole(clazz: KClass<T>): T {
        if (userRoles.containsKey(clazz.java)) throw DomainException("User already has role $clazz")
        val role: UserRole = UserRole.createFor(clazz.java, this)
        userRoles[clazz.java] = role
        return role as T
    }

    override fun <T : UserRole> hasAuthority(clazz: KClass<T>): Boolean {
        return getAllRoles().any { it.javaClass == clazz.java }
    }

    override val account: UserAccount
        @JsonIgnore
        get() = this

    @PreRemove
    fun preRemove() {
        this.payments.forEach { it.user = null }
        this.payments.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserAccount) return false

        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }


    override fun emailDomain(): String {
        return email.split("@").last()
    }
}

class BasicGrantedAuthority(userRole: UserRole) : GrantedAuthority {

    private val internal: String = userRole.authority

    override fun getAuthority(): String? {
        return internal
    }

}
