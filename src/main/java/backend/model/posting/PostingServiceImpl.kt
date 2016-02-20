package backend.model.posting

import backend.model.misc.Coords
import backend.model.user.UserCore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PostingServiceImpl @Autowired constructor(val repository: PostingRepository) : PostingService {

    override fun save(posting: Posting): Posting = repository.save(posting)!!

    override fun findAll(): Iterable<Posting> = repository.findAll()

    override fun createPosting(text: String?, postingLocation: Coords?, user: UserCore, media: MutableList<Media>?): Posting {
        val post = Posting(text, postingLocation, user, media)
        return repository.save(post)
    }

    override fun getByID(id: Long): Posting? {
        return repository.findById(id)
    }
}
