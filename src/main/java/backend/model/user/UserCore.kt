package backend.model.user

import backend.model.BasicEntity
import backend.model.post.Post
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.security.core.GrantedAuthority
import java.util.*

import javax.persistence.*
import java.util.HashMap

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


    // TODO: Not sure how to annotate this one
    @JsonProperty("isBlocked")
    override var isBlocked = false

    override var gender: String? = null

    @OrderColumn
    @OneToMany(cascade = arrayOf(CascadeType.ALL), orphanRemoval = true)
    val posts: MutableList<Post>? = ArrayList()
    /*
     * cascade all operations to children
     * orphanRemoval = true allows removing a role from the database
     * if it gets removed from Map userRoles and the core is saved!
     * See: http://stackoverflow.com/a/2011546
     */
    @OneToMany(cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER, orphanRemoval = true)
    private var userRoles: MutableMap<Class<out UserRole>, UserRole> = HashMap()

    private lateinit var activationToken: String

    fun getAuthorities() : Collection<GrantedAuthority> {
        return this.userRoles.values
    }

    override fun activate(token: String) {
        if(isActivationTokenCorrect(token)) this.isBlocked = false
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

    @Throws(Exception::class)
    override fun addRole(clazz: Class<out UserRole>): UserRole {

        val role: UserRole

        if (userRoles.containsKey(clazz)) {
            throw Exception("User already has role $clazz")
        } else {
            role = UserRole.createFor(clazz, this)
            userRoles.put(clazz, role)
            return role
        }
    }

    override fun getRole(clazz: Class<out UserRole>): UserRole? {
        return userRoles[clazz]
    }

    override fun hasRole(clazz: Class<out UserRole>): Boolean {
        return userRoles.containsKey(clazz)
    }

    override fun removeRole(clazz: Class<out UserRole>): UserRole? {
        if (userRoles.containsKey(clazz)) {
            return userRoles.remove(clazz)
        } else {
            return null
        }
    }

    override val core: UserCore
        @JsonIgnore
        get() = this
}
