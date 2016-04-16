@file:JvmName("Sponsor")

package backend.model.user

import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue("S")
class Sponsor : UserRole {

    var company: String? = null
    var logo: String? = null
    var url: String? = null
    var address: String? = null
    var isHidden: String? = null

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(core: UserCore) : super(core)

    constructor(core: UserCore, company: String, logo: String, url: String, address: String, isHidden: String) : super(core) {
        this.company = company
        this.logo = logo
        this.url = url
        this.address = address
        this.isHidden = isHidden
    }

    override fun getAuthority(): String = "SPONSOR"
}
