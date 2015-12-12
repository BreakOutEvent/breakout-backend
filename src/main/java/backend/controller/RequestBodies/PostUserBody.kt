@file:JvmName("PostUserBody")

package backend.controller.RequestBodies

import org.hibernate.validator.constraints.Email
import org.hibernate.validator.constraints.NotEmpty

class PostUserBody {
    @NotEmpty var firstname: String? = null
    @NotEmpty var lastname: String? = null
    @NotEmpty @Email var email: String? = null
    @NotEmpty var password: String? = null
    @NotEmpty var gender: String? = null
}
