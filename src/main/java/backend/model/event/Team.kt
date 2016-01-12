package backend.model.event

import backend.model.user.Participant
import java.util.*
import javax.persistence.*

@Entity
class Team() {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    val id: Long? = null

    @Column(unique = true)
    lateinit var number: String

    lateinit var name: String

    lateinit var description: String

    val picture: String? = null

    val status: String? = null

    @OneToMany
    val members: MutableSet<Participant> = HashSet()

    fun addMembers(participant: Participant) {
        if (members.size < 2) {
            members.add(participant)
        } else throw Exception("This team already has two members")
    }
}
