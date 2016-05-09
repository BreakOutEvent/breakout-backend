package backend.model.sponsoring

import backend.model.event.Team
import backend.model.user.Sponsor
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SponsoringServiceImpl : SponsoringService {

    private val sponsoringRepository: SponsoringRepository

    @Autowired
    constructor(sponsoringRepository: SponsoringRepository) {
        this.sponsoringRepository = sponsoringRepository
    }

    @Transactional
    override fun createSponsoring(sponsor: Sponsor, team: Team, amountPerKm: Money, limit: Money): Sponsoring {
        val sponsoring = Sponsoring(sponsor, team, amountPerKm, limit)
        return sponsoringRepository.save(sponsoring)
    }

    @Transactional
    override fun acceptSponsoring(sponsoring: Sponsoring): Sponsoring {
        sponsoring.accept()
        return sponsoringRepository.save(sponsoring)
    }

    override fun findByTeamId(teamId: Long) = sponsoringRepository.findByTeamId(teamId)

    override fun findBySponsorId(sponsorId: Long) = sponsoringRepository.findBySponsorCoreId(sponsorId)
}
