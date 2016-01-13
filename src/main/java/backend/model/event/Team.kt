package backend.model.event

import backend.model.user.Participant
import java.util.*
import javax.persistence.*

@Entity
class Team() {

    constructor(creator: Participant, name: String, description: String, status: String = "", number: String = "") : this() {
        this.addMember(creator)
        this.number = number
        this.name = name
        this.description = description
        this.status = status
    }

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    val id: Long? = null

    @Column(unique = true)
    lateinit var number: String

    lateinit var name: String

    lateinit var description: String

    var picture: String? = null

    var status: String? = null

    @OneToMany
    val members: MutableSet<Participant> = HashSet()

    fun addMember(participant: Participant) {
        if (members.size < 2) {
            members.add(participant)
            participant.currentTeam = this
        } else throw Exception("This team already has two members")
    }
}
