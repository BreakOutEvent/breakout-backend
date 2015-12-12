package backend.model.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty

import javax.persistence.*
import java.util.HashMap

@Entity
class UserCore : User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    var id: Long? = null

    @NotEmpty
    override lateinit var passwordHash: String

    @NotEmpty
    override lateinit var firstname: String

    @NotEmpty
    override lateinit var lastname: String

    @Email
    @Column(unique = true, nullable = false)
    override lateinit var email: String

    // TODO: Not sure how to annotate this one
    override var isBlocked = false



    @NotEmpty
    override lateinit var gender: String

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
