@file:JvmName("Sponsor")

package backend.model.user

import backend.model.media.Media
import backend.model.misc.Url
import backend.model.sponsoring.Sponsoring
import javax.persistence.*
import javax.persistence.CascadeType.ALL

@Entity
@DiscriminatorValue("S")
class Sponsor : UserRole {

    var company: String? = null

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var logo: Media

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "team")
    var sponsorings: MutableList<Sponsoring> = arrayListOf()

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
        this.logo = Media("IMAGE")
        this.url = url
        this.address = address
        this.isHidden = isHidden
    }

    override fun getAuthority(): String = "SPONSOR"

    @PreRemove
    fun preRemove() {
        this.sponsorings.forEach { it.sponsor = null }
        this.sponsorings.clear()
    }
}
