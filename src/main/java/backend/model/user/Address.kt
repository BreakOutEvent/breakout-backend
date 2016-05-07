package backend.model.user

import javax.persistence.Embeddable

@Embeddable
class Address {

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(street: String, housenumber: String, city: String, country: String) : this() {
        this.street = street
        this.housenumber = housenumber
        this.city = city
        this.country = country
    }

    lateinit var street: String
    lateinit var housenumber: String
    lateinit var city: String
    lateinit var country: String
}
