package backend.model.user

import backend.model.BasicEntity
import backend.model.posting.Posting
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.security.core.GrantedAuthority
import java.util.*
import javax.persistence.*
import kotlin.reflect.KClass

@Entity
open class UserCore : BasicEntity, User {

    constructor() : super() {
        this.isBlocked = true
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

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    val postings: MutableList<Posting>? = ArrayList()
    /*
     * cascade all operations to children
     * orphanRemoval = true allows removing a role from the database
     * if it gets removed from Map userRoles and the core is saved!
     * See: http://stackoverflow.com/a/2011546
     */
    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, orphanRemoval = true)
    private var userRoles: MutableMap<Class<out UserRole>, UserRole> = HashMap()

    private lateinit var activationToken: String

    fun getAuthorities(): Collection<GrantedAuthority> {
        return this.userRoles.values
    }

    override fun activate(token: String) {
        if (isActivationTokenCorrect(token)) this.isBlocked = false
        else throw Exception("Provided token $token does not match the activation token")
    }

    override fun isActivationTokenCorrect(token: String): Boolean {
        return this.activationToken == token
    }

    override fun createActivationToken(): String {
        this.activationToken = UUID.randomUUID().toString()
        return this.activationToken
    }

    override fun isActivated(): Boolean {
        return !isBlocked;
    }

    override fun <T : UserRole> addRole(clazz: KClass<T>): T = this.addRole(clazz.java) as T
    override fun <T : UserRole> getRole(clazz: KClass<T>): T? = this.getRole(clazz.java) as? T?
    override fun <T : UserRole> hasRole(clazz: KClass<T>): Boolean = this.hasRole(clazz.java)
    override fun <T : UserRole> removeRole(clazz: KClass<T>): T? = this.removeRole(clazz.java) as? T?

    override fun getRole(clazz: Class<out UserRole>): UserRole? = userRoles[clazz]
    override fun hasRole(clazz: Class<out UserRole>): Boolean = userRoles.containsKey(clazz)
    override fun removeRole(clazz: Class<out UserRole>): UserRole? = userRoles.remove(clazz)

    @Throws(Exception::class)
    override fun addRole(clazz: Class<out UserRole>): UserRole {

        val role: UserRole

        if (userRoles.containsKey(clazz)) throw Exception("User already has role $clazz")
        role = UserRole.createFor(clazz, this)
        userRoles.put(clazz, role)
        return role
    }

    override val core: UserCore
        @JsonIgnore
        get() = this
}
