package backend.model.user

import backend.model.BasicEntity
import backend.model.event.Post
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty
import java.util.*

import javax.persistence.*
import java.util.HashMap

@Entity
open class UserCore : BasicEntity(), User {

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
    var userRoles: MutableMap<Class<out UserRole>, UserRole> = HashMap()


    @Throws(Exception::class)
    override fun addRole(clazz: Class<out UserRole>): UserRole {

        val role: UserRole

        if (userRoles.containsKey(clazz)) {
            return userRoles[clazz]!!
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
