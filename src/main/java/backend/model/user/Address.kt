package backend.model.user

import backend.model.BasicEntity
import javax.persistence.Entity

@Entity
class Address() : BasicEntity() {

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
