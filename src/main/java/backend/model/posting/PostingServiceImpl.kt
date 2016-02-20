package backend.model.posting

import backend.model.misc.Coords
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PostingServiceImpl @Autowired constructor(val repository: PostingRepository) : PostingService {
    override fun findAllSince(id: Long): Iterable<Posting> = repository.findAllSince(id)

    override fun findAllByIds(body: List<Long>): Iterable<Posting> = repository.findAll(body)

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(): Iterable<Posting> = repository.findAll()

    override fun createPosting(text: String?, postingLocation: Coords?, user: UserCore, media: MutableList<Media>?): Posting = repository.save(Posting(text, postingLocation, user, media))

    override fun getByID(id: Long): Posting? = repository.findById(id)
}
