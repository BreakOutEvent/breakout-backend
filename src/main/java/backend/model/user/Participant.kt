@file:JvmName("Participant")

package backend.model.user

import backend.exceptions.DomainException
import backend.model.event.Team
import backend.model.location.Location
import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@DiscriminatorValue("PARTICIPANT")
class Participant : UserRole {

    var emergencynumber: String = ""
    var tshirtsize: String? = null
    var hometown: String? = null
    var phonenumber: String? = null
    var birthdate: LocalDate? = null

    @ManyToOne
    private var currentTeam: Team? = null

    @ManyToMany
    private var teams: MutableSet<Team>

    @OneToMany(mappedBy = "uploader")
    val locations: MutableList<Location> = ArrayList()

    /**
     * Private constructor for JPA
     */
    private constructor() : super() {
        teams = mutableSetOf()
    }

    constructor(account: UserAccount) : super(account) {
        teams = mutableSetOf()
    }

    fun getCurrentTeam(): Team? {
        return this.currentTeam
    }

    fun getAllTeams(): Set<Team> {
        return this.teams
    }

    fun clearAllTeams() {
        this.currentTeam = null
        this.teams.clear()
    }

    fun setCurrentTeam(team: Team?) {
        if (team != null) {
            this.teams.add(team)
        }
        this.currentTeam = team
    }

    override fun getAuthority(): String {
        return "PARTICIPANT"
    }

    @PreRemove
    fun preRemove() {
        this.locations.forEach { it.uploader = null }
        this.locations.clear()
    }

    fun removeTeam(team: Team) {
        if (this.getCurrentTeam() == team) {
            this.setCurrentTeam(null)
        }

        if (!this.teams.remove(team)) {
            throw DomainException("Can't remove team ${team.id} from participant $id. " +
                    "The participant has never been a part of this team")
        }
    }
}
