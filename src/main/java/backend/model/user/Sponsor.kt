@file:JvmName("Sponsor")

package backend.model.user

import backend.model.challenges.Challenge
import backend.model.media.Media
import backend.model.misc.Url
import backend.model.sponsoring.ISponsor
import backend.model.sponsoring.Sponsoring
import backend.model.sponsoring.SupporterType
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

    @Enumerated(EnumType.STRING)
    override var supporterType: SupporterType = SupporterType.NONE

    override var company: String? = null

    @OneToOne(cascade = [ALL], orphanRemoval = true)
    override var logo: Media? = null

    @OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "registeredSponsor")
    override var sponsorings: MutableList<Sponsoring> = arrayListOf()

    @OneToMany(cascade = [ALL], orphanRemoval = true, mappedBy = "registeredSponsor")
    override var challenges: MutableList<Challenge> = arrayListOf()

    @Embedded
    override var url: Url? = null

    @Embedded
    override var address: Address? = null

    override var isHidden: Boolean = false

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(account: UserAccount) : super(account) {
        this.account = account
    }

    constructor(account: UserAccount, supporterType: SupporterType, company: String, url: Url, address: Address,
                isHidden: Boolean, logo: Media?) : super(account) {
        this.supporterType = supporterType
        this.company = company
        this.logo = logo
        this.url = url
        this.address = address
        this.isHidden = isHidden
        this.account = account
    }

    @PreRemove
    fun preRemove() {
        this.sponsorings.forEach(Sponsoring::removeSponsors)
        this.sponsorings.clear()

        this.challenges.forEach(Challenge::removeSponsor)
        this.sponsorings.clear()
    }

    override fun getAuthority(): String = "SPONSOR"
}
