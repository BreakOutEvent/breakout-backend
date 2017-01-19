package backend.model.sponsoring

import backend.model.BasicEntity
import backend.model.challenges.Challenge
import backend.model.user.Address
import backend.model.user.Sponsor
import backend.model.user.UserAccount
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

    lateinit var gender: String

    lateinit var url: String

    @Embedded
    override lateinit var address: Address

    @OneToMany(mappedBy = "unregisteredSponsor")
    var sponsorings: MutableList<Sponsoring> = mutableListOf()

    @OneToMany(mappedBy = "unregisteredSponsor")
    var challenges: MutableList<Challenge> = mutableListOf()

    @Column(nullable = true) // TODO: Why nullable?
    override
    var isHidden: Boolean = false

    /**
     * private no-args constructor for JPA / Hibernate
     */
    private constructor()

    constructor(firstname: String,
                lastname: String,
                company: String,
                gender: String,
                url: String,
                address: Address,
                isHidden: Boolean = false) {

        this.firstname = firstname
        this.lastname = lastname
        this.company = company
        this.gender = gender
        this.url = url
        this.address = address
        this.isHidden = isHidden
    }
}
