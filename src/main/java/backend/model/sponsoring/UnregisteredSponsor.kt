package backend.model.sponsoring

import backend.model.BasicEntity
import backend.model.challenges.Challenge
import backend.model.misc.Url
import backend.model.user.Address
import backend.model.user.Sponsor
import javax.persistence.*

@Entity
class UnregisteredSponsor : BasicEntity, ISponsor {

    @Transient
    override var registeredSponsor: Sponsor? = null
        set(value) {}

    @Transient
    override var unregisteredSponsor: UnregisteredSponsor? = this
        set(value) {}

    override var firstname: String? = null

    override var lastname: String? = null

    override var company: String? = null

    @Enumerated(EnumType.STRING)
    override var supporterType: SupporterType = SupporterType.DONOR

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "url"))
    override var url: Url? = null

    @Embedded
    override lateinit var address: Address

    @OneToMany(mappedBy = "unregisteredSponsor")
    override var sponsorings: MutableList<Sponsoring> = mutableListOf()

    @OneToMany(mappedBy = "unregisteredSponsor")
    override var challenges: MutableList<Challenge> = mutableListOf()

    @Column(nullable = true) // TODO: Why nullable?
    override
    var isHidden: Boolean = false

    var email: String? = null

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor()

    constructor(firstname: String,
                lastname: String,
                company: String,
                gender: String? = null,
                url: String? = null,
                address: Address,
                isHidden: Boolean = false,
                email: String? = null) {

        this.firstname = firstname
        this.lastname = lastname
        this.company = company
        this.address = address
        this.isHidden = isHidden
        this.email = email
    }
}
