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
    override var sponsorRole: Sponsor? = this
        set(value) {}

    @Transient
    override var unregisteredSponsor: UnregisteredSponsor? = null
        set(value) {}

    // TODO: This is also a hack!
    @Transient
    override var userAccount: UserAccount? = null
        set(value) {}

    override var company: String? = null

    @OneToOne(cascade = arrayOf(ALL), orphanRemoval = true)
    lateinit var logo: Media

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "sponsor")
    var sponsorings: MutableList<Sponsoring> = arrayListOf()

    @OneToMany(cascade = arrayOf(ALL), orphanRemoval = true, mappedBy = "registeredSponsor")
    var challenges: MutableList<Challenge> = arrayListOf()

    @Embedded
    var url: Url? = null

    @Embedded
    override lateinit var address: Address

    override var isHidden: Boolean = false

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account)

    constructor(account: UserAccount, company: String, logo: String, url: Url, address: Address, isHidden: Boolean) : super(account) {
        this.company = company
        this.logo = Media("IMAGE")
        this.url = url
        this.address = address
        this.isHidden = isHidden
        this.userAccount = account
    }

    override fun getAuthority(): String = "SPONSOR"

    @PreRemove
    fun preRemove() {
        this.sponsorings.forEach { it.sponsor = null }
        this.sponsorings.clear()

        this.challenges.forEach(Challenge::removeSponsors)
        this.sponsorings.clear()
    }
}
