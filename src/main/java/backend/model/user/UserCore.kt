package backend.model.user

import backend.exceptions.DomainException
import backend.model.BasicEntity
import backend.model.media.Media
import backend.model.messaging.GroupMessage
import backend.model.messaging.Message
import backend.model.payment.Payment
import backend.model.posting.Comment
import backend.model.posting.Posting
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import javax.persistence.*
import kotlin.reflect.KClass

@Entity
open class UserCore : BasicEntity, User {

    constructor() : super() {
        this.isBlocked = true
        this.profilePic = Media("image")
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

    @OneToOne(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    override var profilePic: Media

    @OneToMany(mappedBy = "user", cascade = arrayOf(CascadeType.ALL))
    val postings: MutableList<Posting> = ArrayList()

    @OneToMany(mappedBy = "user", cascade = arrayOf(CascadeType.ALL))
    val comments: MutableList<Comment> = ArrayList()

    @ManyToMany(mappedBy = "users", cascade = arrayOf(CascadeType.ALL))
    val groupMessages: MutableList<GroupMessage> = ArrayList()

    @OneToMany(mappedBy = "creator", cascade = arrayOf(CascadeType.ALL))
    val messages: MutableList<Message> = ArrayList()

    @OneToMany(mappedBy = "user", cascade = arrayOf(CascadeType.ALL))
    val payments: MutableList<Payment> = ArrayList()

    /*
     * cascade all operations to children
     * orphanRemoval = true allows removing a role from the database
     * if it gets removed from Map userRoles and the core is saved!
     * See: http://stackoverflow.com/a/2011546
     */
    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, orphanRemoval = true)
    private var userRoles: MutableMap<Class<out UserRole>, UserRole> = HashMap()

    private var activationToken: String? = null

    fun getAuthorities(): Collection<GrantedAuthority> {
        return this.userRoles.values.map { BasicGrantedAuthority(it) }
    }

    override fun activate(token: String) {
        if (isActivationTokenCorrect(token)) {
            this.isBlocked = false
            this.activationToken = null;
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
        return !isBlocked;
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
        val role: UserRole
        if (userRoles.containsKey(clazz.java)) throw DomainException("User already has role $clazz")
        role = UserRole.createFor(clazz.java, this)
        userRoles.put(clazz.java, role)
        return role as T
    }

    override val core: UserCore
        @JsonIgnore
        get() = this

    @PreRemove
    fun preRemove() {
        this.payments.forEach { it.user = null }
        this.payments.clear()
    }
}

class BasicGrantedAuthority : GrantedAuthority {

    private val internal: String

    constructor(userRole: UserRole) {
        this.internal = userRole.authority
    }

    override fun getAuthority(): String? {
        return internal
    }
}
