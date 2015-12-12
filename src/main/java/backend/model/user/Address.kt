package backend.model.user

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
public class Address() {

    constructor(street: String, housenumber: String, city: String, country: String) : this() {
        this.street = street
        this.housenumber = housenumber
        this.city = city
        this.country = country
    }

    @Id
    @GeneratedValue
    var id: Int? = null
    lateinit var street: String
    lateinit var housenumber: String
    lateinit var city: String
    lateinit var country: String
}
