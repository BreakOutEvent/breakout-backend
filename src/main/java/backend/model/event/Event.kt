package backend.model.event

import backend.model.BasicEntity
import backend.model.misc.Coord
import org.javamoney.moneta.Money
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class Event : BasicEntity {

    lateinit var title: String
    lateinit var date: LocalDateTime
    lateinit var city: String
    var isCurrent: Boolean = false
    var isOpenForRegistration: Boolean = false
    var allowNewSponsoring: Boolean = false

    @OneToMany(mappedBy = "event")
    var teams: MutableList<Team> = ArrayList()

    @Embedded
    lateinit var startingLocation: Coord

    var duration: Int = 36

    var teamFee: Money? = Money.of(60, "EUR")

    var brand: String? = null
    var bank: String? = null
    var iban: String? = null
    var bic: String? = null

    /**
     * Private constructor for JPA
     */
    private constructor() : super()

    constructor(title: String, date: LocalDateTime, city: String, startingLocation: Coord, duration: Int, teamFee: Money, brand: String, bank: String, iban: String, bic: String) : this() {
        this.title = title
        this.date = date
        this.city = city
        this.startingLocation = startingLocation
        this.duration = duration
        this.teamFee = teamFee
        this.brand = brand
        this.bank = bank
        this.iban = iban
        this.bic = bic
    }

}
