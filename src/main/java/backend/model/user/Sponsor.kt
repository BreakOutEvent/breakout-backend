@file:JvmName("Sponsor")

package backend.model.user

import backend.model.media.Media
import backend.model.misc.Url
import javax.persistence.CascadeType.ALL
import javax.persistence.DiscriminatorValue
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.OneToOne

@Entity
@DiscriminatorValue("S")
class Sponsor : UserRole {

    var company: String? = null

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    var logo: Media? = null

    @Embedded
    var url: Url? = null

    @Embedded
    var address: Address? = null

    var isHidden: Boolean = false

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(core: UserCore) : super(core)

    constructor(core: UserCore, company: String, logo: String, url: Url, address: Address, isHidden: Boolean) : super(core) {
        this.company = company
//        this.logo = logo //TODO: Checkout how to integrate this with media
        this.url = url
        this.address = address
        this.isHidden = isHidden
    }

    override fun getAuthority(): String = "SPONSOR"
}
