@file:JvmName("PostUserBody")

package backend.controller.RequestBodies

import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty

class PostUserBody() {

    constructor(firstname: String?, lastname: String?, email: String?, password: String?, gender: String?) : this() {
        this.firstname = firstname
        this.lastname = lastname
        this.email = email
        this.password = password
        this.gender = gender
    }

    var firstname: String? = null
    var lastname: String? = null
    @NotEmpty @Email var email: String? = null
    @NotEmpty var password: String? = null
    var gender: String? = null
}
