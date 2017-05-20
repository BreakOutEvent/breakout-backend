@file:JvmName("Sponsor")

package backend.model.user

import backend.model.challenges.Challenge
import backend.model.media.Media
import backend.model.misc.Url
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.UnregisteredSponsor
import javax.persistence.*
import javax.persistence.CascadeType.ALL

@Entity
@DiscriminatorValue("S")
class Sponsor : UserRole, ISponsor {

    @Transient
    override var registeredSponsor: Sponsor? = this
        set(value) {}

    @Transient
    override var unregisteredSponsor: UnregisteredSponsor? = null
        set(value) {}

    override var company: String? = null

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var logo: Media

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "registeredSponsor")
    override var sponsorings: MutableList<Sponsoring> = arrayListOf()

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "registeredSponsor")
    override var challenges: MutableList<Challenge> = arrayListOf()

    @Embedded
    override var url: Url? = null

    @Embedded
    override lateinit var address: Address

    override var isHidden: Boolean = false

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account) {
        this.account = account
    }

    constructor(account: UserAccount, company: String, logo: String, url: Url, address: Address, isHidden: Boolean) : super(account) {
        this.company = company
        this.logo = Media("IMAGE")
        this.url = url
        this.address = address
        this.isHidden = isHidden
        this.account = account
    }

    override fun getAuthority(): String = "SPONSOR"

    @PreRemove
    fun preRemove() {
        this.sponsorings.forEach(Sponsoring::removeSponsors)
        this.sponsorings.clear()

        this.challenges.forEach(Challenge::removeSponsor)
        this.sponsorings.clear()
    }
}
