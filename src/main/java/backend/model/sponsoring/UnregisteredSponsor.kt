package backend.model.sponsoring

import backend.model.user.Address
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
class UnregisteredSponsor {

    lateinit var firstname: String
    lateinit var lastname: String
    lateinit var company: String
    lateinit var gender: String
    lateinit var url: String
    @Embedded lateinit var address: Address
    @Column(nullable = true) var isHidden: Boolean = false

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
